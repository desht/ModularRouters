package me.desht.modularrouters.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.modularrouters.logic.filter.Filter.Flags;

public class SetofItemStack extends ObjectOpenCustomHashSet<ItemStack> {

    private static class ItemStackHashingStrategy implements Strategy<ItemStack> {
        private final Flags filterFlags;

        ItemStackHashingStrategy(Flags filterFlags) {
            this.filterFlags = filterFlags;
        }

        @Override
        public int hashCode(ItemStack object) {
            int hashCode = Item.getIdFromItem(object.getItem());
            if (!filterFlags.isIgnoreDamage()) hashCode += 37 * object.getDamage();
            if (!filterFlags.isIgnoreNBT() && object.hasTag()) hashCode += 37 * object.getTag().hashCode();
            return hashCode;
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            return (o1 == o2) || !(o1 == null || o2 == null)
                    && o1.getItem() == o2.getItem()
                    && (filterFlags.isIgnoreDamage() || o1.getDamage() == o2.getDamage())
                    && (filterFlags.isIgnoreNBT() || !o1.hasTag() || o1.getTag().equals(o2.getTag()));
        }
    }

    public SetofItemStack(Flags filterFlags) {
        super(new ItemStackHashingStrategy(filterFlags));
    }

    public SetofItemStack(Collection<? extends ItemStack> collection, Flags filterFlags) {
        super(collection, new ItemStackHashingStrategy(filterFlags));
    }

    public static SetofItemStack fromItemHandler(IItemHandler handler, Flags filterFlags) {
        NonNullList<ItemStack> itemStacks = NonNullList.create();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                itemStacks.add(stack);
            }
        }
        return new SetofItemStack(itemStacks, filterFlags);
    }

    public List<ItemStack> sortedList() {
        return this.stream().sorted(compareStacks).collect(Collectors.toList());
    }

    private static final Comparator<? super ItemStack> compareStacks = (Comparator<ItemStack>) (o1, o2) -> {
        // matches by mod, then by display name
        int c = o1.getItem().getRegistryName().getNamespace().compareTo(o2.getItem().getRegistryName().getNamespace());
        if (c != 0) return c;
        return o1.getDisplayName().getString().compareTo(o2.getDisplayName().getString());
    };
}
