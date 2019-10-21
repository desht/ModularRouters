package me.desht.modularrouters.logic.filter.matchers;

import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidMatcher implements IItemMatcher {
    private final Fluid fluid;

    public FluidMatcher(ItemStack stack) {
        fluid = FluidUtil.getFluidContained(stack).map(FluidStack::getFluid).orElse(null);
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        return FluidUtil.getFluidContained(stack)
                .map(fluidStack -> fluidStack.getFluid() == fluid)
                .orElse(false);
    }

    public boolean matchFluid(Fluid fluid) {
        return fluid == this.fluid;
    }
}
