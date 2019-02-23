package me.desht.modularrouters.util;

import com.google.common.collect.Sets;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class that allows ItemStacks to be used in sets & maps.
 */
public class HashableItemStackWrapper {
    private final ItemStack stack;
    private final Filter.Flags filterFlags;

    public HashableItemStackWrapper(ItemStack stack) {
        this(stack, Filter.Flags.DEFAULT_FLAGS);
    }

    public HashableItemStackWrapper(@Nonnull ItemStack stack, Filter.Flags filterFlags) {
        this.stack = stack;
        this.filterFlags = filterFlags;
    }

    @Nonnull
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashableItemStackWrapper)) return false;
        HashableItemStackWrapper that = (HashableItemStackWrapper) o;
        return this.stack.getItem() == that.stack.getItem()
                && (filterFlags.isIgnoreMeta() || this.stack.getDamage() == that.stack.getDamage())
                && (filterFlags.isIgnoreNBT() || !this.stack.hasTag() || this.stack.getTag().equals(that.stack.getTag()));
    }

    @Override
    public int hashCode() {
        int hashCode = Item.getIdFromItem(stack.getItem());
        if (!filterFlags.isIgnoreMeta()) hashCode += 37 * stack.getDamage();
        if (!filterFlags.isIgnoreNBT() && stack.hasTag()) hashCode += 37 * stack.getTag().hashCode();
        return hashCode;
    }

    public static class CountedItemStacks extends HashMap<HashableItemStackWrapper, Integer> {
        public static CountedItemStacks fromItemHandler(IItemHandler handler) {
            CountedItemStacks res = new CountedItemStacks();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    res.compute(new HashableItemStackWrapper(stack), (wrapper, n) -> wrapper.stack.getCount() + (n == null ? 0 : n));
                }
            }
            return res;
        }
    }

    public static Set<HashableItemStackWrapper> makeSet(IItemHandler handler, Filter.Flags filterFlags) {
        Set<HashableItemStackWrapper> res = Sets.newHashSet();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                res.add(new HashableItemStackWrapper(stack, filterFlags));
            }
        }
        return res;
    }

    public static List<ItemStack> sortedList(Set<HashableItemStackWrapper> set) {
        return set.stream().map(HashableItemStackWrapper::getStack).sorted(compareStacks).collect(Collectors.toList());
    }

    private static final Comparator<? super ItemStack> compareStacks = (Comparator<ItemStack>) (o1, o2) -> {
        // matches by mod, then by display name
        int c = o1.getItem().getRegistryName().getNamespace().compareTo(o2.getItem().getRegistryName().getNamespace());
        if (c != 0) return c;
        return o1.getDisplayName().getString().compareTo(o2.getDisplayName().getString());
    };
}
