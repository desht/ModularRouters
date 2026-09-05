package me.desht.modularrouters.integration.mekanism;

import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.item.module.FluidModule1;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ChemicalModule1 extends FluidModule1 {
    private static final TintColor TINT_COLOR = new TintColor(190, 120, 240);

    public ChemicalModule1() {
        super(CompiledChemicalModule::new);
    }

    @Override
    public boolean supportsWorldInteraction() {
        return false;
    }

    @Override
    public String getTransferHelpPrefix() {
        return "modularrouters.guiText.popup.chemical.";
    }

    @Override
    public String getTransferTooltipKey() {
        return "modularrouters.guiText.tooltip.chemicalTransferTooltip";
    }

    @Override
    protected String getTransferRateKey() {
        return "modularrouters.itemText.chemical.maxTransfer";
    }

    @Override
    public ItemStack getTransferTargetIcon() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mekanism", "basic_chemical_tank")));
    }

    @Override
    public boolean isItemValidForFilter(ItemStack stack) {
        if (stack.isEmpty()) return true;
        IChemicalHandler handler = MekanismIntegration.getItemHandler(stack);
        if (handler != null) {
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                if (!handler.getChemicalInTank(tank).isEmpty()) return true;
            }
        }
        return false;
    }

    @Override
    public IItemMatcher getFilterItemMatcher(ItemStack stack) {
        return new ChemicalMatcher(stack);
    }

    @Override
    protected Component getFilterItemDisplayName(ItemStack stack) {
        IChemicalHandler handler = MekanismIntegration.getItemHandler(stack);
        if (handler != null) {
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                if (!handler.getChemicalInTank(tank).isEmpty()) {
                    return handler.getChemicalInTank(tank).getTextComponent();
                }
            }
        }
        return stack.getHoverName();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }
}
