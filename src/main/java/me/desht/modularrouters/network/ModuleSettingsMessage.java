package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Used when a player updates the flags on a router module via its GUI.
 */
public class ModuleSettingsMessage implements IMessage {
    private byte flags;
    private BlockPos routerPos;
    private int slotIndex;
    private EnumHand hand;

    public ModuleSettingsMessage() {
    }

    public ModuleSettingsMessage(byte flags, BlockPos routerPos, int slotIndex, EnumHand hand) {
        this.flags = flags;
        this.routerPos = routerPos;
        this.slotIndex = slotIndex;
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        flags = byteBuf.readByte();
        byte routerData = byteBuf.readByte();
        if (routerData == 1) {
            routerPos = new BlockPos(byteBuf.readInt(), byteBuf.readInt(), byteBuf.readInt());
            slotIndex = byteBuf.readInt();
            hand = EnumHand.MAIN_HAND;
        } else {
            hand = EnumHand.values()[byteBuf.readInt()];
            routerPos = null;
            slotIndex = -1;
        }
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeByte(flags);
        if (routerPos != null) {
            byteBuf.writeByte(1);
            byteBuf.writeInt(routerPos.getX());
            byteBuf.writeInt(routerPos.getY());
            byteBuf.writeInt(routerPos.getZ());
            byteBuf.writeInt(slotIndex);
        } else {
            byteBuf.writeByte(0);
            byteBuf.writeInt(hand.ordinal());
        }
    }

    public static class Handler implements IMessageHandler<ModuleSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(ModuleSettingsMessage msg, MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
            mainThread.addScheduledTask(() -> {
                // get the new setting into the module item, which could either be held by the player
                // or installed in an item router
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                TileEntityItemRouter router = getRouter(player.getEntityWorld(), msg);
                ItemStack stack = router == null ?
                        player.getHeldItem(msg.hand) :
//                        router.getModules().getStackInSlot(msg.slotIndex);
                        router.getModules().extractItem(msg.slotIndex, 1, false);
                if (stack != null && stack.getItem() instanceof ItemModule) {
                    NBTTagCompound compound = stack.getTagCompound();
                    compound.setByte("Flags", msg.flags);
                    if (router != null) {
                        router.getModules().insertItem(msg.slotIndex, stack, false);
                        router.recompileNeeded();
                    }
                } else {
                    ModularRouters.logger.warn("player not holding expected item router module!  ignoring attempt to change settings");
                }

            });
            return null;
        }

        private TileEntityItemRouter getRouter(World w, ModuleSettingsMessage msg) {
            if (msg.routerPos == null) {
                return null;
            }
            TileEntity te = w.getTileEntity(msg.routerPos);
            return te instanceof TileEntityItemRouter ? (TileEntityItemRouter) te : null;
        }
    }

}
