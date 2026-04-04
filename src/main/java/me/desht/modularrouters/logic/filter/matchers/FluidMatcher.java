package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.api.matching.IModuleFlags;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

public class FluidMatcher implements IItemMatcher {
    private final Fluid fluid;

    public FluidMatcher(ItemStack stack) {
        fluid = FluidUtil.getFirstStackContained(stack).getFluid();
    }

    @Override
    public boolean matchItem(ItemStack stack, IModuleFlags flags) {
        return matchFluid(FluidUtil.getFirstStackContained(stack).getFluid(), flags);
    }

    @Override
    public boolean matchFluid(Fluid fluid, IModuleFlags flags) {
        return fluid == this.fluid || flags.matchItemTags() && !Sets.intersection(MiscUtil.fluidTags(fluid), MiscUtil.fluidTags(this.fluid)).isEmpty();
    }

}
