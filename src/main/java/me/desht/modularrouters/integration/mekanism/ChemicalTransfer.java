package me.desht.modularrouters.integration.mekanism;

import me.desht.modularrouters.ModularRouters;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;

import java.util.function.Predicate;

public final class ChemicalTransfer {
    public static long transfer(IChemicalHandler source, IChemicalHandler destination, long limit,
                                Predicate<ChemicalStack> filter) {
        if (limit <= 0 || source == destination) return 0;
        for (int tank = 0; tank < source.getChemicalTanks(); tank++) {
            ChemicalStack available = source.extractChemical(tank, limit, Action.SIMULATE);
            if (available.isEmpty() || !filter.test(available)) continue;

            ChemicalStack remainder = destination.insertChemical(available, Action.SIMULATE);
            long accepted = available.getAmount() - remainder.getAmount();
            if (accepted <= 0) continue;

            ChemicalStack extracted = source.extractChemical(tank, accepted, Action.EXECUTE);
            if (extracted.isEmpty()) continue;
            remainder = destination.insertChemical(extracted, Action.EXECUTE);
            long transferred = extracted.getAmount() - remainder.getAmount();
            if (!remainder.isEmpty()) {
                ChemicalStack unrecovered = source.insertChemical(tank, remainder, Action.EXECUTE);
                if (!unrecovered.isEmpty()) {
                    ModularRouters.LOGGER.error("Chemical handler changed its transfer result and rejected rollback of {}", unrecovered);
                }
            }
            return transferred;
        }
        return 0;
    }

    private ChemicalTransfer() {
    }
}
