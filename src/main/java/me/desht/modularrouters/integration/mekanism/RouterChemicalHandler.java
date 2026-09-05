package me.desht.modularrouters.integration.mekanism;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
final class RouterChemicalHandler implements IChemicalHandler {
    private final ModularRouterBlockEntity router;

    RouterChemicalHandler(ModularRouterBlockEntity router) {
        this.router = router;
    }

    private IChemicalHandler delegate() {
        return MekanismIntegration.getItemHandler(router.getBufferItemStack());
    }

    @Override
    public int getChemicalTanks() {
        IChemicalHandler handler = delegate();
        return handler == null ? 0 : handler.getChemicalTanks();
    }

    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        IChemicalHandler handler = delegate();
        return valid(handler, tank) ? handler.getChemicalInTank(tank) : ChemicalStack.EMPTY;
    }

    @Override
    public void setChemicalInTank(int tank, ChemicalStack stack) {
        throw new UnsupportedOperationException("Use insertChemical/extractChemical to modify the router buffer");
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        IChemicalHandler handler = delegate();
        return valid(handler, tank) ? handler.getChemicalTankCapacity(tank) : 0;
    }

    @Override
    public boolean isValid(int tank, ChemicalStack stack) {
        IChemicalHandler handler = delegate();
        return valid(handler, tank) && handler.isValid(tank, stack);
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        IChemicalHandler handler = delegate();
        if (!valid(handler, tank)) return stack;
        ChemicalStack remainder = handler.insertChemical(tank, stack, action);
        if (action.execute() && remainder.getAmount() < stack.getAmount()) {
            router.setBufferItemStack(router.getBufferItemStack());
        }
        return remainder;
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        IChemicalHandler handler = delegate();
        if (!valid(handler, tank)) return ChemicalStack.EMPTY;
        ChemicalStack extracted = handler.extractChemical(tank, amount, action);
        if (action.execute() && !extracted.isEmpty()) {
            router.setBufferItemStack(router.getBufferItemStack());
        }
        return extracted;
    }

    private static boolean valid(IChemicalHandler handler, int tank) {
        return handler != null && tank >= 0 && tank < handler.getChemicalTanks();
    }
}
