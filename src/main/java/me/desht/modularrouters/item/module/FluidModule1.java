package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule1;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.filter.matchers.FluidMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class FluidModule1 extends ItemModule {
    public FluidModule1() {
        super(ModItems.defaultProps());
    }

    public enum FluidDirection {
        IN,  // to router
        OUT;  // from router

        public String getTranslationKey() {
            return "itemText.fluid.direction." + toString();
        }
    }

    @Override
    public String getRegulatorTranslationKey(ItemStack stack) {
        CompoundNBT compound = ModuleHelper.validateNBT(stack);
        boolean isAbsolute = compound.getBoolean(CompiledFluidModule1.NBT_REGULATE_ABSOLUTE);
        return "guiText.tooltip.regulator." + (isAbsolute ? "labelFluidmB" : "labelFluidPct");
    }

    @Override
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_FLUID.get();
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledFluidModule1(router, stack);
    }

    @Override
    protected ITextComponent getFilterItemDisplayName(ItemStack stack) {
        return FluidUtil.getFluidContained(stack).map(FluidStack::getDisplayName).orElse(stack.getDisplayName());
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<ITextComponent> list) {
        super.addExtraInformation(stack, list);

        addFluidModuleInformation(stack, list);
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
    public TintColor getItemTint() {
        return new TintColor(79, 191, 255);
    }

    static void addFluidModuleInformation(ItemStack stack, List<ITextComponent> list) {
        CompiledFluidModule1 cfm = new CompiledFluidModule1(null, stack);
        String dir = I18n.format("itemText.fluid.direction." + cfm.getFluidDirection());
        list.add(xlate("itemText.fluid.direction", dir));
        list.add(xlate("itemText.fluid.maxTransfer", cfm.getMaxTransfer()));
    }
}
