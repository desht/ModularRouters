package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.client.gui.widgets.IResyncableGui;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 *
 * Sent when a filter is changed server-side which requires an open GUI to re-read its settings.
 */
public class GuiSyncMessage {
    private ItemStack newStack;

    public GuiSyncMessage() {
    }

    public GuiSyncMessage(ItemStack newStack) {
        this.newStack = newStack;
    }

    public GuiSyncMessage(ByteBuf buf) {
        newStack = new PacketBuffer(buf).readItemStack();
    }

    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeItemStack(newStack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().field_71462_r instanceof IResyncableGui) {
                ((IResyncableGui) Minecraft.getInstance().field_71462_r).resync(newStack);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
