package me.desht.modularrouters.logic.filter.matchers;

import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.world.item.ItemStack;

public interface IItemMatcher {
    boolean matchItem(ItemStack stack, Filter.Flags flags);
}
