package me.desht.modularrouters.logic.filter.matchers;

import me.desht.modularrouters.logic.filter.Filter;
import mekanism.api.chemical.gas.Gas;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public interface IItemMatcher {
    boolean matchItem(ItemStack stack, Filter.Flags flags);

    default boolean matchFluid(Fluid fluid, Filter.Flags flags) {
        return false;
    }

    default boolean matchGas(Gas gas, Filter.Flags flags) {
        return false;
    }
}
