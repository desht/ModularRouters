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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Used when a player updates the flags on a router module via its GUI.
 */
public class ModuleSettingsMessage implements IMessage {
    private byte flags;
    private RouterRedstoneBehaviour rbb;
    private BlockPos routerPos;
    private int slotIndex;
    private EnumHand hand;
    private NBTTagCompound extData;

    public ModuleSettingsMessage() {
    }

    public ModuleSettingsMessage(byte flags, RouterRedstoneBehaviour rbb, BlockPos routerPos, int slotIndex, EnumHand hand, NBTTagCompound extData) {
        this.flags = flags;
        this.rbb = rbb;
        this.routerPos = routerPos;
        this.slotIndex = slotIndex;
        this.hand = hand;
        this.extData = extData;
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        flags = byteBuf.readByte();
        rbb = RouterRedstoneBehaviour.values()[byteBuf.readByte()];
        boolean routerData = byteBuf.readBoolean();
        if (routerData) {
            hand = EnumHand.MAIN_HAND;
            routerPos = new BlockPos(byteBuf.readInt(), byteBuf.readInt(), byteBuf.readInt());
            slotIndex = byteBuf.readInt();
        } else {
            hand = EnumHand.values()[byteBuf.readInt()];
            routerPos = null;
            slotIndex = -1;
        }
        extData = ByteBufUtils.readTag(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeByte(flags);
        byteBuf.writeByte(rbb.ordinal());
        if (routerPos != null) {
            byteBuf.writeBoolean(true);
            byteBuf.writeInt(routerPos.getX());
            byteBuf.writeInt(routerPos.getY());
            byteBuf.writeInt(routerPos.getZ());
            byteBuf.writeInt(slotIndex);
        } else {
            byteBuf.writeBoolean(false);
            byteBuf.writeInt(hand.ordinal());
        }
        ByteBufUtils.writeTag(byteBuf, extData);
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
                        router.getModules().extractItem(msg.slotIndex, 1, false);
                if (stack != null && stack.getItem() instanceof ItemModule) {
                    NBTTagCompound compound = stack.getTagCompound();
                    compound.setByte(Module.NBT_FLAGS, msg.flags);
                    compound.setString(Module.NBT_REDSTONE_MODE, msg.rbb.toString());
                    if (msg.extData != null) {
                        // extended data set by certain modules; copy directly into the item's NBT
                        // e.g. the redstone settings for the detector module
                        for (String key : msg.extData.getKeySet()) {
                            compound.setTag(key, msg.extData.getTag(key));
                        }
                    }
                    if (router != null) {
                        router.getModules().insertItem(msg.slotIndex, stack, false);
                        router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
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
