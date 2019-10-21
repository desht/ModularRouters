package me.desht.modularrouters.logic.filter.matchers;

import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.ItemTagMatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class SimpleItemMatcher implements IItemMatcher {
    private final ItemStack filterStack;
    private final ItemTagMatcher tagMatcher;

    public SimpleItemMatcher(ItemStack stack) {
        this.filterStack = stack;
        tagMatcher = new ItemTagMatcher(stack);
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        if (Item.getIdFromItem(filterStack.getItem()) != Item.getIdFromItem(stack.getItem())
                || !flags.isIgnoreDamage() && filterStack.getDamage() != stack.getDamage()) {
            return !flags.isIgnoreTags() && tagMatcher.match(stack);
        }
        if (!flags.isIgnoreNBT()) {
            CompoundNBT filterTag = filterStack.getTag();
            CompoundNBT stackTag = stack.getTag();
            if (filterTag == null && stackTag != null || filterTag != null && stackTag == null) {
                return false;
            } else if (filterTag == null) {
                return true;
            } else if (!filterTag.contains("GEN")) {
                return filterTag.equals(stackTag);
            } else {
                CompoundNBT filterTag2 = filterTag.copy();
                CompoundNBT itemTag2 = stackTag.copy();
                filterTag2.remove("GEN");
                itemTag2.remove("GEN");
                return filterTag2.equals(itemTag2);
            }
        }
        return true;
    }
}
