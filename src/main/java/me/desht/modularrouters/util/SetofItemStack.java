package me.desht.modularrouters.util;

import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.HashingStrategy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SetofItemStack extends TCustomHashSet<ItemStack> {
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

    public SetofItemStack() {
        super(new ItemStackHashingStrategy());
    }

    public SetofItemStack(int initialCapacity) {
        super(new ItemStackHashingStrategy(), initialCapacity);
    }

    public SetofItemStack(int initialCapacity, float loadFactor) {
        super(new ItemStackHashingStrategy(), initialCapacity, loadFactor);
    }

    public SetofItemStack(Collection<? extends ItemStack> collection) {
        super(new ItemStackHashingStrategy(), collection);
    }

    public List<ItemStack> sortedList() {
        return this.stream().sorted(compareStacks).collect(Collectors.toList());
    }

    private static Comparator<? super ItemStack> compareStacks = new Comparator<ItemStack>() {
        @Override
        public int compare(ItemStack o1, ItemStack o2) {
            // matches by mod, then by display name
            int c = o1.getItem().getRegistryName().getResourceDomain().compareTo(o2.getItem().getRegistryName().getResourceDomain());
            if (c != 0) return c;
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
    };
}
