package me.desht.modularrouters.util;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class CodecUtil {
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackHandler> ITEM_HANDLER_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ItemStackHandler decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            int slotCount = buf.readVarInt();
            List<SlottedItem> slots = new ArrayList<>(slotCount);
            for (int i = 0; i < slotCount; i++) {
                int slot = buf.readVarInt();
                ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
                slots.add(new SlottedItem(slot, stack));
            }

            return createHandler(size, slots);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ItemStackHandler handler) {
            List<SlottedItem> slots = new ArrayList<>();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    slots.add(new SlottedItem(i, stack));
                }
            }
            buf.writeVarInt(handler.getSlots());
            buf.writeVarInt(slots.size());
            slots.forEach(rec -> {
                buf.writeVarInt(rec.slot);
                ItemStack.STREAM_CODEC.encode(buf, rec.stack);
            });
        }
    };

    private static ItemStackHandler createHandler(int handlerSize, List<SlottedItem> slots) {
        ItemStackHandler h = new ItemStackHandler(handlerSize);
        slots.forEach(rec -> {
            h.setStackInSlot(rec.slot, rec.stack);
        });
        return h;
    }

    private record SlottedItem(int slot, ItemStack stack) {
        public static final StreamCodec<RegistryFriendlyByteBuf, SlottedItem> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SlottedItem::slot,
                ItemStack.STREAM_CODEC, SlottedItem::stack,
                SlottedItem::new
        );
    }
}
