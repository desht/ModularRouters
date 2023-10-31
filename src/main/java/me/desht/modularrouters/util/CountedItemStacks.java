package me.desht.modularrouters.util;

import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class CountedItemStacks extends Object2IntOpenCustomHashMap<ItemStack> {
    private static class ItemStackHashingStrategy implements Strategy<ItemStack> {
        @Override
        public int hashCode(ItemStack object) {
            return 31 * Item.getId(object.getItem()) + object.getDamageValue();
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            return (o1 == o2) || !(o1 == null || o2 == null)
                    && o1.getItem() == o2.getItem()
                    && o1.getDamageValue() == o2.getDamageValue();
            // ignore NBT for these purposes
        }
    }

    public CountedItemStacks() {
        super(new ItemStackHashingStrategy());
    }

    public CountedItemStacks(IItemHandler handler) {
        super(handler.getSlots(), new ItemStackHashingStrategy());

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                put(stack, getOrDefault(stack, 0) + stack.getCount());
            }
        }
    }
}
