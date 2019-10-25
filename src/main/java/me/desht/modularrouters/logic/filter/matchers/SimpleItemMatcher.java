package me.desht.modularrouters.logic.filter.matchers;

import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.ItemTagMatcher;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.Validate;

public class SimpleItemMatcher implements IItemMatcher {
    private final ItemStack filterStack;
    private ItemTagMatcher tagMatcher;

    public SimpleItemMatcher(ItemStack stack) {
        Validate.isTrue(!stack.isEmpty());
        this.filterStack = stack;
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        if (filterStack.getItem() == stack.getItem()) {
            return (flags.isIgnoreDamage() || matchDamage(stack, filterStack))
                    && (flags.isIgnoreNBT() || ItemStack.areItemStackTagsEqual(stack, filterStack));
        } else if (flags.matchTags()) {
            return getTagMatcher().match(stack);
        } else {
            return false;
        }
    }

    private ItemTagMatcher getTagMatcher() {
        if (tagMatcher == null) tagMatcher = new ItemTagMatcher(filterStack);
        return tagMatcher;
    }


    private boolean matchDamage(ItemStack stack1, ItemStack stack2) {
        // items are already checked to be the same, only interested in durability here
        return !stack1.isDamageable() || stack1.getDamage() == stack2.getDamage();
    }
}
