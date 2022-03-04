package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule2;
import me.desht.modularrouters.logic.filter.matchers.FluidMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.List;

public class FluidModule2 extends TargetedModule implements IRangedModule, IPositionProvider {

    private static final TintColor TINT_COLOR = new TintColor(64, 224, 255);

    public FluidModule2() {
        super(ModItems.defaultProps(), CompiledFluidModule2::new);
    }

    @Override
    protected boolean isValidTarget(UseOnContext ctx) {
        return !ctx.getLevel().isEmptyBlock(ctx.getClickedPos());
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public MenuType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_FLUID.get();
    }

    @Override
    protected Component getFilterItemDisplayName(ItemStack stack) {
        return FluidUtil.getFluidContained(stack).map(FluidStack::getDisplayName).orElse(stack.getHoverName());
    }

    @Override
    public boolean isItemValidForFilter(ItemStack stack) {
        // only fluid-holding items or a smart filter item can go into a fluid module's filter
        if (stack.isEmpty() || stack.getItem() instanceof SmartFilterItem) return true;
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
        return ConfigHolder.common.module.fluid2BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHolder.common.module.fluid2MaxRange.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<Component> list) {
        super.addExtraInformation(stack, list);
        FluidModule1.addFluidModuleInformation(stack, list);
    }

    @Override
    public int getRenderColor(int index) {
        return 0x8040E0FF;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.fluidModule2EnergyCost.get();
    }

}
