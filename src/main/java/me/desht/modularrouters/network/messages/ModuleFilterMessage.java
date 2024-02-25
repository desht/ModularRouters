package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Received on: SERVER
 * <p>
 * Sent by client when a filter slot is updated via the JEI ghost handler
 */
public record ModuleFilterMessage(int slot, ItemStack stack) implements CustomPacketPayload {
    public static final ResourceLocation ID = MiscUtil.RL("module_filter");

    public ModuleFilterMessage(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readItem());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(slot);
        buffer.writeItem(stack);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
