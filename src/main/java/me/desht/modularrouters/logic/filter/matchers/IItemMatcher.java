package me.desht.modularrouters.logic.filter.matchers;

import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public interface IItemMatcher {
    boolean matchItem(ItemStack stack, Filter.Flags flags);

    default boolean matchFluid(Fluid fluid, Filter.Flags flags) {
        return false;
    }
}
