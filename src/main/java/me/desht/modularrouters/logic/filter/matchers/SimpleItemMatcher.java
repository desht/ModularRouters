package me.desht.modularrouters.logic.filter.matchers;

import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.api.matching.IModuleFlags;
import me.desht.modularrouters.util.ItemTagMatcher;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;

public class SimpleItemMatcher implements IItemMatcher {
    private final ItemStack filterStack;
    private ItemTagMatcher tagMatcher;

    public SimpleItemMatcher(ItemStack stack) {
        Validate.isTrue(!stack.isEmpty());
        this.filterStack = stack;
    }

    @Override
    public boolean matchItem(ItemStack stack, IModuleFlags flags, HolderLookup.Provider registryAccess) {
        if (filterStack.getItem() == stack.getItem()) {
            return (!flags.matchDamage() || matchDamage(stack, filterStack))
                    && (!flags.matchComponents() || ItemStack.isSameItemSameComponents(stack, filterStack));
        } else if (flags.matchItemTags()) {
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
        return !stack1.isDamageableItem() || stack1.getDamageValue() == stack2.getDamageValue();
    }
}
