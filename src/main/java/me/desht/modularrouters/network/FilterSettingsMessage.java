package me.desht.modularrouters.network;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent when a filter's settings have been changed in any way via its GUI.
 * The filter could be in a player's hand, or in a module (which may or may not be in a router...)
 */
public class FilterSettingsMessage {
    private final Operation op;
    private final CompoundTag payload;
    private final MFLocator locator;

    public enum Operation {
        CLEAR_ALL, REMOVE_ITEM, MERGE, LOAD, ADD_STRING, REMOVE_AT, ANY_ALL_FLAG
    }

    public FilterSettingsMessage(Operation op, MFLocator locator, CompoundTag payload) {
        this.op = op;
        this.locator = locator;
        this.payload = payload;
    }

    public FilterSettingsMessage(FriendlyByteBuf buf) {
        op = buf.readEnum(Operation.class);
        locator = MFLocator.fromBuffer(buf);
        payload = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(op);
        locator.writeBuf(buf);
        buf.writeNbt(payload);
    }

    public Operation getOp() {
        return op;
    }

    public CompoundTag getPayload() {
        return payload;
    }

    public LazyOptional<IItemHandler> getTargetInventory() {
        ModuleTarget target = ModuleTarget.fromNBT(payload);
        return target.getItemHandler();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                processPacket(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void processPacket(ServerPlayer player) {
        ItemStack moduleStack = locator.getModuleStack(player);
        ItemStack filterStack = locator.getTargetItem(player);
        if (filterStack.getItem() instanceof SmartFilterItem) {
            SmartFilterItem sf = (SmartFilterItem) filterStack.getItem();
            GuiSyncMessage response = sf.onReceiveSettingsMessage(player, this, filterStack, moduleStack);
            if (!moduleStack.isEmpty()) {
                ModularRouterBlockEntity router = locator.getRouter(player.level).orElse(null);
                ModuleFilterHandler filterHandler = new ModuleFilterHandler(moduleStack, router);
                filterHandler.setStackInSlot(locator.filterSlot, filterStack);
                filterHandler.save();
                if (locator.hand != null) {
                    player.setItemInHand(locator.hand, filterHandler.getHolderStack());
                } else if (router != null) {
                    router.recompileNeeded(ModularRouterBlockEntity.COMPILE_MODULES);
                }
            }
            if (response != null) {
                // send to any nearby players in case they also have the GUI open
                PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(player.getX(), player.getY(), player.getZ(),
                        8, player.getCommandSenderWorld().dimension());
                PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp), response);
            }
        }
    }
}
