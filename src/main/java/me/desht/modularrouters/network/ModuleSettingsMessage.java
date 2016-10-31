package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Used when a player updates the flags on a router module via its GUI.
 */
public class ModuleSettingsMessage extends BaseSettingsMessage {
    public ModuleSettingsMessage() {
    }

    public ModuleSettingsMessage(BlockPos routerPos, int moduleSlotIndex, EnumHand hand, NBTTagCompound data) {
        super(routerPos, hand, moduleSlotIndex, data);
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        super.fromBytes(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        super.toBytes(byteBuf);
    }

    public static class Handler implements IMessageHandler<ModuleSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(ModuleSettingsMessage msg, MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
            mainThread.addScheduledTask(() -> {
                // Get the new settings into the module item, which could either be held by the player
                // or installed in an item router
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                ItemStack moduleStack = null;
                if (msg.routerPos != null) {
                    TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(player.getEntityWorld(), msg.routerPos);
                    if (router != null) {
                        moduleStack = router.getModules().getStackInSlot(msg.moduleSlotIndex);
                        router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
                    }
                } else if (msg.hand != null) {
                    moduleStack = player.getHeldItem(msg.hand);
                }
                // All settings for the module are encoded in NBT data
                if (ItemModule.getModule(moduleStack) != null) {
                    NBTTagCompound compound = ModuleHelper.validateNBT(moduleStack);
                    for (String key : msg.nbtData.getKeySet()) {
                        compound.setTag(key, msg.nbtData.getTag(key));
                    }
                } else {
                    ModularRouters.logger.warn("ignoring ModuleSettingsMessage for " + player.getDisplayName() + " - expected module not found");
                }

            });
            return null;
        }
    }

}
