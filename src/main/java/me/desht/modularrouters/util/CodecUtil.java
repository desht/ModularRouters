package me.desht.modularrouters.util;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;

import java.util.ArrayList;
import java.util.List;

public class CodecUtil {
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStacksResourceHandler> ITEM_HANDLER_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ItemStacksResourceHandler decode(RegistryFriendlyByteBuf buf) {
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
        public void encode(RegistryFriendlyByteBuf buf, ItemStacksResourceHandler handler) {
            List<SlottedItem> slots = new ArrayList<>();
            for (int i = 0; i < handler.size(); i++) {
                ItemStack stack = ItemUtil.getStack(handler, i);
                if (!stack.isEmpty()) {
                    slots.add(new SlottedItem(i, stack));
                }
            }
            buf.writeVarInt(handler.size());
            buf.writeVarInt(slots.size());
            slots.forEach(rec -> {
                buf.writeVarInt(rec.slot);
                ItemStack.STREAM_CODEC.encode(buf, rec.stack);
            });
        }
    };

    private static ItemStacksResourceHandler createHandler(int handlerSize, List<SlottedItem> slots) {
        return Util.make(new ItemStacksResourceHandler(handlerSize), h ->
                slots.forEach(rec -> h.set(rec.slot, ItemResource.of(rec.stack), rec.stack.getCount())));
    }

    private record SlottedItem(int slot, ItemStack stack) {
        public static final StreamCodec<RegistryFriendlyByteBuf, SlottedItem> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SlottedItem::slot,
                ItemStack.STREAM_CODEC, SlottedItem::stack,
                SlottedItem::new
        );
    }
}
