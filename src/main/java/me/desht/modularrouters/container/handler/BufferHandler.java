package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BufferHandler extends ItemStackHandler {
    private final ModularRouterBlockEntity router;

    private IEnergyStorage energyStorage;
    private IFluidHandlerItem fluidHandler;

    public BufferHandler(ModularRouterBlockEntity router) {
        super(router.getBufferSlotCount());
        this.router = router;

        setupFluidAndEnergyCaps();
    }

    @Override
    public void onContentsChanged(int slot) {
        ItemStack stack = getStackInSlot(slot);

        IFluidHandlerItem newFluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        IEnergyStorage newEnergyStorage = stack.getCapability(Capabilities.EnergyStorage.ITEM);

        if (newFluidHandler != fluidHandler || newEnergyStorage != energyStorage) {
            fluidHandler = newFluidHandler;
            energyStorage = newEnergyStorage;

            router.invalidateCapabilities();

            // in case any pipes/cables need to connect/disconnect
            router.nonNullLevel().updateNeighborsAt(router.getBlockPos(), ModBlocks.MODULAR_ROUTER.get());
        }

        router.setChanged();  // will also update comparator output
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);

        setupFluidAndEnergyCaps();
    }

    public IFluidHandlerItem getFluidHandler() {
        return fluidHandler;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private void setupFluidAndEnergyCaps() {
        ItemStack stack = getStackInSlot(0);

        fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        energyStorage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
    }
}
