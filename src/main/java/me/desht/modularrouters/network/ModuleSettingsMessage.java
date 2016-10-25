package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
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
    private byte flags;
    private RouterRedstoneBehaviour rrb;

    public ModuleSettingsMessage() {
    }

    public ModuleSettingsMessage(byte flags, RouterRedstoneBehaviour rrb, BlockPos routerPos, int moduleSlotIndex, EnumHand hand, NBTTagCompound extData) {
        super(routerPos, hand, moduleSlotIndex, extData);
        this.flags = flags;
        this.rrb = rrb;
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        flags = byteBuf.readByte();
        rrb = RouterRedstoneBehaviour.values()[byteBuf.readByte()];
        super.fromBytes(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeByte(flags);
        byteBuf.writeByte(rrb.ordinal());
        super.toBytes(byteBuf);
    }

    public static class Handler implements IMessageHandler<ModuleSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(ModuleSettingsMessage msg, MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
            mainThread.addScheduledTask(() -> {
                // get the new setting into the module item, which could either be held by the player
                // or installed in an item router
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                TileEntityItemRouter router = msg.routerPos == null ? null : TileEntityItemRouter.getRouterAt(player.getEntityWorld(), msg.routerPos);
                ItemStack moduleStack = router == null ?
                        player.getHeldItem(msg.hand) :
                        router.getModules().getStackInSlot(msg.moduleSlotIndex);
                if (ItemModule.getModule(moduleStack) != null) {
                    NBTTagCompound compound = moduleStack.getTagCompound();
                    if (compound != null) {
                        compound.setByte(Module.NBT_FLAGS, msg.flags);
                        compound.setString(Module.NBT_REDSTONE_MODE, msg.rrb.toString());
                        if (msg.extData != null) {
                            // extended data set by certain modules; copy directly into the item's NBT
                            // e.g. the redstone settings for the detector module
                            for (String key : msg.extData.getKeySet()) {
                                compound.setTag(key, msg.extData.getTag(key));
                            }
                        }
                    }
                } else {
                    ModularRouters.logger.warn("player not holding expected item router module!  ignoring attempt to change settings");
                }

            });
            return null;
        }
    }

}
