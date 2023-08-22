package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.IHasTranslationKey;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.compiled.CompiledGasModule1;
import me.desht.modularrouters.logic.filter.matchers.GasMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.util.ModuleHelper;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;
import static me.desht.modularrouters.core.ModItems.getGasHandler;

public class GasModule1 extends ModuleItem {

    private static final TintColor TINT_COLOR = new TintColor(79, 191, 255);

    public GasModule1() {
        super(ModItems.defaultProps(), CompiledGasModule1::new);
    }

    public enum GasDirection implements IHasTranslationKey {
        IN,  // to router
        OUT;  // from router

        @Override
        public String getTranslationKey() {
            return "modularrouters.itemText.gas.direction." + this;
        }
    }

    @Override
    public String getRegulatorTranslationKey(ItemStack stack) {
        CompoundTag compound = ModuleHelper.validateNBT(stack);
        boolean isAbsolute = compound.getBoolean(CompiledGasModule1.NBT_REGULATE_ABSOLUTE);
        return "modularrouters.guiText.tooltip.regulator." + (isAbsolute ? "labelGasmB" : "labelGasPct");
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.GAS_MENU.get();
    }

    @Override
    protected Component getFilterItemDisplayName(ItemStack stack) {
       // return FluidUtil.getFluidContained(stack).map(ChemicalStack: getDisplayName).orElse(stack.getHoverName());
        return ItemStack.EMPTY.getDisplayName();
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<Component> list) {
        super.addExtraInformation(stack, list);

        addGasModuleInformation(stack, list);
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.gasModuleEnergyCost.get();
    }

    public static Optional<GasStack> getGasContained(@NotNull ItemStack container)
    {
        if (!container.isEmpty())
        {
            container = ItemHandlerHelper.copyStackWithSize(container, 1);
            Optional<GasStack> gasContained = getGasHandler(container)
                    .map(handler -> handler.extractChemical(Integer.MAX_VALUE, Action.SIMULATE));
            if (gasContained.isPresent() && !gasContained.get().isEmpty())
            {
                return gasContained;
            }
        }
        return Optional.empty();
    }


    @Override
    public boolean isItemValidForFilter(ItemStack stack) {
        // only gas-holding items or a smart filter item can go into a gas module's filter
        if (stack.isEmpty() || stack.getItem() instanceof SmartFilterItem) return true;
        if (stack.getCount() > 1) return false;

        return getGasContained(stack).map(gasStack -> !gasStack.isEmpty()).orElse(false);
    }

    @Override
    public IItemMatcher getFilterItemMatcher(ItemStack stack) {
        return new GasMatcher(stack);
    }

    @Override
    public boolean isFluidModule() {
        return false;
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    static void addGasModuleInformation(ItemStack stack, List<Component> list) {
        CompiledGasModule1 cfm = new CompiledGasModule1(null, stack);
        String dir = I18n.get("modularrouters.itemText.gas.direction." + cfm.getGasDirection());
        list.add(xlate("modularrouters.itemText.gas.direction", dir));
        list.add(xlate("modularrouters.itemText.gas.maxTransfer", cfm.getMaxTransfer()));
    }
}
