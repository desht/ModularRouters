package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule2;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.filter.matchers.FluidMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.List;

public class FluidModule2 extends TargetedModule implements IRangedModule, IPositionProvider {
    public FluidModule2() {
        super(ModItems.defaultProps());
    }

    @Override
    protected boolean isValidTarget(ItemUseContext ctx) {
        return !ctx.getWorld().isAirBlock(ctx.getPos());
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_FLUID.get();
    }

    @Override
    protected ITextComponent getFilterItemDisplayName(ItemStack stack) {
        return FluidUtil.getFluidContained(stack).map(FluidStack::getDisplayName).orElse(stack.getDisplayName());
    }

    @Override
    public boolean isItemValidForFilter(ItemStack stack) {
        // only fluid-holding items or a smart filter item can go into a fluid module's filter
        if (stack.isEmpty() || stack.getItem() instanceof ItemSmartFilter) return true;
        if (stack.getCount() > 1) return false;

        return FluidUtil.getFluidContained(stack).map(fluidStack -> !fluidStack.isEmpty()).orElse(false);
    }

    @Override
    public IItemMatcher getFilterItemMatcher(ItemStack stack) {
        return new FluidMatcher(stack);
    }

    @Override
    public boolean isFluidModule() {
        return true;
    }

    @Override
    public int getBaseRange() {
        return MRConfig.Common.Module.fluid2BaseRange;
    }

    @Override
    public int getHardMaxRange() {
        return MRConfig.Common.Module.fluid2MaxRange;
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledFluidModule2(router, stack);
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(64, 224, 255);
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<ITextComponent> list) {
        super.addExtraInformation(stack, list);
        FluidModule1.addFluidModuleInformation(stack, list);
    }

    @Override
    public int getRenderColor(int index) {
        return 0x8040E0FF;
    }
}
