package me.desht.modularrouters.network;

import me.desht.modularrouters.client.util.ClientUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.simple.SimpleMessage;

/**
 * Received on: CLIENT
 *
 * Sent when a filter is changed server-side which requires an open GUI to re-read its settings.
 */
public class GuiSyncMessage implements SimpleMessage {
    private final ItemStack newStack;

    public GuiSyncMessage(ItemStack newStack) {
        this.newStack = newStack;
    }

    public GuiSyncMessage(FriendlyByteBuf buf) {
        newStack = buf.readItem();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeItem(newStack);
    }

    @Override
    public void handleMainThread(NetworkEvent.Context context) {
        ClientUtil.maybeGuiSync(newStack);
    }
}
