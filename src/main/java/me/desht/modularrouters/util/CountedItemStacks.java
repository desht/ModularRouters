package me.desht.modularrouters.util;

import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.strategy.HashingStrategy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class CountedItemStacks extends TCustomHashMap<ItemStack, Integer> {
    private static class ItemStackHashingStrategy implements HashingStrategy<ItemStack> {
        @Override
        public int computeHashCode(ItemStack object) {
            return 31 * Item.getIdFromItem(object.getItem()) + object.getItemDamage();
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            return (o1 == o2) || !(o1 == null || o2 == null)
                    && o1.getItem() == o2.getItem()
                    && o1.getItemDamage() == o2.getItemDamage();
            // ignore NBT for these purposes
        }
    }

    public CountedItemStacks() {
        super(new ItemStackHashingStrategy());
    }

    public CountedItemStacks(IItemHandler handler) {
        super(new ItemStackHashingStrategy(), handler.getSlots());

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack != null) {
                put(stack, getOrDefault(stack, 0) + stack.stackSize);
            }
        }
    }
}
