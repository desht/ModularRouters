package me.desht.modularrouters.network;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent by client when a player updates a module's settings via its GUI.
 */
public class ModuleSettingsMessage {
    private final MFLocator locator;
    private final CompoundTag payload;

    ModuleSettingsMessage(FriendlyByteBuf buf) {
        locator = MFLocator.fromBuffer(buf);
        payload = buf.readNbt();
    }

    public ModuleSettingsMessage(MFLocator locator, CompoundTag payload) {
        this.locator = locator;
        this.payload = payload;
    }

    public void toBytes(FriendlyByteBuf buf) {
        locator.writeBuf(buf);
        buf.writeNbt(payload);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack moduleStack = locator.getModuleStack(player);

                if (moduleStack.getItem() instanceof ModuleItem) {
                    CompoundTag compound = ModuleHelper.validateNBT(moduleStack);
                    for (String key : payload.getAllKeys()) {
                        compound.put(key, payload.get(key));
                    }
                    if (locator.routerPos != null) {
                        player.getCommandSenderWorld().getBlockEntity(locator.routerPos, ModBlockEntities.MODULAR_ROUTER.get())
                                .ifPresent(router -> router.recompileNeeded(ModularRouterBlockEntity.COMPILE_MODULES));
                    }
                } else {
                    ModularRouters.LOGGER.warn("ignoring ModuleSettingsMessage for " + player.getDisplayName().getString() + " - expected module not found @ " + locator.toString());
                }
            }
        });

        ctx.get().setPacketHandled(true);
    }

}
