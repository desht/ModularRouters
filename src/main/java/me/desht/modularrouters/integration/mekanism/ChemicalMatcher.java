package me.desht.modularrouters.integration.mekanism;

import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.api.matching.IModuleFlags;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class ChemicalMatcher implements IItemMatcher {
    private final List<ChemicalStack> chemicals = new ArrayList<>();

    ChemicalMatcher(ItemStack stack) {
        IChemicalHandler handler = MekanismIntegration.getItemHandler(stack);
        if (handler != null) {
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                ChemicalStack chemical = handler.getChemicalInTank(tank);
                if (!chemical.isEmpty()) chemicals.add(chemical.copyWithAmount(1));
            }
        }
    }

    boolean matches(ChemicalStack stack, IModuleFlags flags) {
        return !stack.isEmpty() && chemicals.stream().anyMatch(filter ->
                ChemicalStack.isSameChemical(filter, stack)
                        || flags.matchItemTags() && filter.getTags().anyMatch(stack::is));
    }

    @Override
    public boolean matchItem(ItemStack stack, IModuleFlags flags, HolderLookup.Provider registryAccess) {
        IChemicalHandler handler = MekanismIntegration.getItemHandler(stack);
        if (handler != null) {
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                if (matches(handler.getChemicalInTank(tank), flags)) return true;
            }
        }
        return false;
    }
}
