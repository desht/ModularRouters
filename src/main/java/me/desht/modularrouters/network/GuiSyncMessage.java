package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.client.gui.widgets.IResyncableGui;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GuiSyncMessage implements IMessage {
    private ItemStack newStack;

    public GuiSyncMessage() {
    }

    public GuiSyncMessage(ItemStack newStack) {
        this.newStack = newStack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        newStack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, newStack);
    }

    public static class Handler implements IMessageHandler<GuiSyncMessage, IMessage> {
        @Override
        public IMessage onMessage(GuiSyncMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (Minecraft.getMinecraft().currentScreen instanceof IResyncableGui) {
                    ((IResyncableGui) Minecraft.getMinecraft().currentScreen).resync(message.newStack);
                }
            });
            return null;
        }
    }
}
