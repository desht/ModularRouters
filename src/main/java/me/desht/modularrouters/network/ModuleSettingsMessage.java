package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ModuleSettingsMessage implements IMessage {
    private byte flags;

    public ModuleSettingsMessage() {
    }

    public ModuleSettingsMessage(byte flags) {
        this.flags = flags;
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        flags = byteBuf.readByte();
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeByte(flags);
    }

    public static class Handler implements IMessageHandler<ModuleSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(ModuleSettingsMessage msg, MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
            mainThread.addScheduledTask(() -> {
                // get the new setting into the module item that the player should still be holding
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
                if (stack != null && stack.getItem() instanceof ItemModule) {
                    NBTTagCompound compound = stack.getTagCompound();
                    compound.setByte("Flags", msg.flags);
                } else {
                    ModularRouters.logger.warn("player not holding expected item router module!  ignoring attempt to change settings");
                }

            });
            return null;
        }
    }
}
