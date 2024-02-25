package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Received on: CLIENT
 * <p>
 * Sent when a filter is changed server-side which requires an open GUI to re-read its settings.
 */
public record GuiSyncMessage(ItemStack newStack) implements CustomPacketPayload {
    public static final ResourceLocation ID = MiscUtil.RL("gui_sync");

    public GuiSyncMessage(FriendlyByteBuf buf) {
        this(buf.readItem());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeItem(newStack);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
