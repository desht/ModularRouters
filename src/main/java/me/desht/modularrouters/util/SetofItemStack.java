package me.desht.modularrouters.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import me.desht.modularrouters.logic.filter.Filter.Flags;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class SetofItemStack extends ObjectOpenCustomHashSet<ItemStack> {
    private record ItemStackHashingStrategy(Flags filterFlags) implements Strategy<ItemStack> {
        @Override
        public int hashCode(ItemStack object) {
            int hashCode = Item.getId(object.getItem());
            if (!filterFlags.isIgnoreDamage()) hashCode += 37 * object.getDamageValue();
            if (!filterFlags.isIgnoreNBT() && object.hasTag()) //noinspection ConstantConditions
                hashCode += 37 * object.getTag().hashCode();
            return hashCode;
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            //noinspection ConstantConditions
            return (o1 == o2) || !(o1 == null || o2 == null)
                    && o1.getItem() == o2.getItem()
                    && (filterFlags.isIgnoreDamage() || o1.getDamageValue() == o2.getDamageValue())
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
                itemStacks.add(stack.copy());
            }
        }
        return new SetofItemStack(itemStacks, filterFlags);
    }

    public List<ItemStack> sortedList() {
        return this.stream().sorted(COMPARE_STACKS).toList();
    }

    // matches by mod, then by display name
    private static final Comparator<? super ItemStack> COMPARE_STACKS = Comparator
            .comparing((ItemStack stack) -> namespace(stack.getItem()))
            .thenComparing(stack -> stack.getHoverName().getString());

    private static String namespace(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getNamespace();
    }
}
