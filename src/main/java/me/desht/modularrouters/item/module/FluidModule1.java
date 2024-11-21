package me.desht.modularrouters.item.module;

import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule.FluidModuleSettings;
import me.desht.modularrouters.logic.filter.matchers.FluidMatcher;
import net.minecraft.ChatFormatting;
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
        super(ModItems.defaultProps(), CompiledFluidModule::new);
    }

    @Override
    public String getRegulatorTranslationKey(ItemStack stack) {
        boolean isAbsolute = stack.getOrDefault(ModDataComponents.FLUID_SETTINGS, FluidModuleSettings.DEFAULT).regulateAbsolute();
        return "modularrouters.guiText.tooltip.regulator." + (isAbsolute ? "labelFluidmB" : "labelFluidPct");
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.FLUID_MENU.get();
    }

    @Override
    protected Component getFilterItemDisplayName(ItemStack stack) {
        return FluidUtil.getFluidContained(stack).map(FluidStack::getHoverName).orElse(stack.getHoverName());
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<Component> list) {
        super.addExtraInformation(stack, list);

        FluidModuleSettings settings = stack.getOrDefault(ModDataComponents.FLUID_SETTINGS.get(), FluidModuleSettings.DEFAULT);
        list.add(xlate("modularrouters.itemText.transfer_direction",
                xlate(settings.direction().getTranslationKey()).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.YELLOW));
        list.add(xlate("modularrouters.itemText.fluid.maxTransfer",
                colorText(settings.maxTransfer(), ChatFormatting.AQUA)).withStyle(ChatFormatting.YELLOW));
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
}
