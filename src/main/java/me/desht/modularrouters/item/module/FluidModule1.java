package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule1;
import me.desht.modularrouters.logic.filter.matchers.FluidMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.util.ModuleHelper;
import me.desht.modularrouters.util.TranslatableEnum;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.colorText;
import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class FluidModule1 extends ModuleItem {

    private static final TintColor TINT_COLOR = new TintColor(79, 191, 255);

    public FluidModule1() {
        super(ModItems.defaultProps(), CompiledFluidModule1::new);
    }

    public enum FluidDirection implements TranslatableEnum {
        IN,  // to router
        OUT;  // from router

        @Override
        public String getTranslationKey() {
            return "modularrouters.itemText.fluid.direction." + this;
        }
    }

    @Override
    public String getRegulatorTranslationKey(ItemStack stack) {
        CompoundTag compound = ModuleHelper.validateNBT(stack);
        boolean isAbsolute = compound.getBoolean(CompiledFluidModule1.NBT_REGULATE_ABSOLUTE);
        return "modularrouters.guiText.tooltip.regulator." + (isAbsolute ? "labelFluidmB" : "labelFluidPct");
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.FLUID_MENU.get();
    }

    @Override
    protected Component getFilterItemDisplayName(ItemStack stack) {
        return FluidUtil.getFluidContained(stack).map(FluidStack::getDisplayName).orElse(stack.getHoverName());
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<Component> list) {
        super.addExtraInformation(stack, list);

        addFluidModuleInformation(stack, list);
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.fluidModuleEnergyCost.get();
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
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    static void addFluidModuleInformation(ItemStack stack, List<Component> list) {
        CompiledFluidModule1 cfm = new CompiledFluidModule1(null, stack);
        list.add(xlate("modularrouters.itemText.fluid.direction",
                xlate(cfm.getFluidDirection().getTranslationKey()).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.YELLOW));
        list.add(xlate("modularrouters.itemText.fluid.maxTransfer",
                colorText(cfm.getMaxTransfer(), ChatFormatting.AQUA)).withStyle(ChatFormatting.YELLOW));
    }
}
