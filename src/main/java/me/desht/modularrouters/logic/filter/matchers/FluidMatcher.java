package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.api.matching.IModuleFlags;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class FluidMatcher implements IItemMatcher {
    private final Fluid fluid;

    public FluidMatcher(ItemStack stack) {
        fluid = FluidUtil.getFluidContained(stack).map(FluidStack::getFluid).orElse(Fluids.EMPTY);
    }

    @Override
    public boolean matchItem(ItemStack stack, IModuleFlags flags, HolderLookup.Provider registryAccess) {
        return FluidUtil.getFluidContained(stack)
                .map(fluidStack -> matchFluid(fluidStack.getFluid(), flags, registryAccess))
                .orElse(false);
    }

    @Override
    public boolean matchFluid(Fluid fluid, IModuleFlags flags, HolderLookup.Provider registryAccess) {
        return fluid == this.fluid || flags.matchItemTags() && !Sets.intersection(MiscUtil.fluidTags(fluid), MiscUtil.fluidTags(this.fluid)).isEmpty();
    }

}
