package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent by client when a player updates a module's settings via its GUI.
 */
public class ModuleSettingsMessage extends BaseSettingsMessage {
    public ModuleSettingsMessage() {
    }

    public ModuleSettingsMessage(BlockPos routerPos, EnumHand hand, NBTTagCompound data) {
        super(routerPos, hand, data);
    }

    public ModuleSettingsMessage(ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        super.toBytes(byteBuf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            EntityPlayer player = ctx.get().getSender();
            ItemStack moduleStack = ItemStack.EMPTY;
            SlotTracker tracker = SlotTracker.getInstance(player);
            if (routerPos != null) {
                TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(player.getEntityWorld(), routerPos);
                if (router != null) {
                    moduleStack = tracker.getConfiguringModule(router);
                    router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
                }
            } else if (hand != null) {
                moduleStack = player.getHeldItem(hand);
            }
            // All settings for the module are encoded in NBT data
            if (moduleStack.getItem() instanceof ItemModule) {
                NBTTagCompound compound = ModuleHelper.validateNBT(moduleStack);
                for (String key : nbtData.keySet()) {
                    compound.put(key, nbtData.get(key));
                }
            } else {
                ModularRouters.LOGGER.warn("ignoring ModuleSettingsMessage for " + player.getDisplayName().getString() + " - expected module not found");
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
