package me.desht.modularrouters.logic.filter.matchers;

import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidMatcher implements IItemMatcher {
    private final Fluid fluid;

    public FluidMatcher(ItemStack stack) {
        if (stack != null) {
            FluidStack fluidStack = FluidUtil.getFluidContained(stack);
            this.fluid = fluidStack != null ? fluidStack.getFluid() : null;
        } else {
            this.fluid = null;
        }
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        FluidStack fStack2 = stack == null ? null : FluidUtil.getFluidContained(stack);
        return fStack2 != null && fStack2.getFluid() == fluid;
    }

    public boolean matchFluid(Fluid fluid) {
        return fluid == this.fluid;
    }
}
