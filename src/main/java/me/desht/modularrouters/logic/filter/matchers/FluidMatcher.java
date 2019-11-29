package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidMatcher implements IItemMatcher {
    private final Fluid fluid;

    public FluidMatcher(ItemStack stack) {
        fluid = FluidUtil.getFluidContained(stack).map(FluidStack::getFluid).orElse(Fluids.EMPTY);
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        return FluidUtil.getFluidContained(stack)
                .map(fluidStack -> matchFluid(fluidStack.getFluid(), flags))
                .orElse(false);
    }

    public boolean matchFluid(Fluid fluid, Filter.Flags flags) {
        return fluid == this.fluid || flags.matchTags() && !Sets.intersection(fluid.getTags(), this.fluid.getTags()).isEmpty();
    }
}
