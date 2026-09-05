package me.desht.modularrouters.integration.mekanism;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule;
import me.desht.modularrouters.logic.settings.TransferDirection;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public class CompiledChemicalModule extends CompiledFluidModule {
    public CompiledChemicalModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        if (getTarget() == null || getTarget().gPos.pos().equals(router.getBlockPos())) return false;

        IChemicalHandler buffer = MekanismIntegration.getItemHandler(router.getBufferItemStack());
        IChemicalHandler target = getTarget().getCapability(MekanismIntegration.BLOCK_CHEMICAL).orElse(null);
        if (buffer == null || target == null) return false;

        boolean pulling = getFluidDirection() == TransferDirection.TO_ROUTER;
        IChemicalHandler source = pulling ? target : buffer;
        IChemicalHandler destination = pulling ? buffer : target;
        int limit = Math.min(getMaxTransfer(), router.getCurrentFluidTransferAllowance(getFluidDirection()));
        limit = regulatedLimit(target, pulling, limit);
        if (limit <= 0) return false;

        long transferred = ChemicalTransfer.transfer(source, destination, limit,
                chemical -> getFilter().testCustom(matcher -> matcher instanceof ChemicalMatcher cm
                        && cm.matches(chemical, getFilter().getFlags())));
        if (transferred > 0) {
            router.transferredFluid((int) transferred, getFluidDirection());
            router.setBufferItemStack(router.getBufferItemStack());
        }
        return transferred > 0;
    }

    private int regulatedLimit(IChemicalHandler target, boolean pulling, int limit) {
        if (getRegulationAmount() <= 0) return limit;

        BigInteger stored = BigInteger.ZERO;
        BigInteger capacity = BigInteger.ZERO;
        for (int tank = 0; tank < target.getChemicalTanks(); tank++) {
            stored = stored.add(BigInteger.valueOf(target.getChemicalInTank(tank).getAmount()));
            capacity = capacity.add(BigInteger.valueOf(target.getChemicalTankCapacity(tank)));
        }
        BigInteger threshold = BigInteger.valueOf(getRegulationAmount());
        if (!isRegulateAbsolute()) threshold = capacity.multiply(threshold.min(BigInteger.valueOf(100))).divide(BigInteger.valueOf(100));
        BigInteger available = pulling ? stored.subtract(threshold) : threshold.subtract(stored);
        return available.max(BigInteger.ZERO).min(BigInteger.valueOf(Math.max(0, limit))).intValue();
    }
}
