package me.desht.modularrouters.util;

import cofh.redstoneflux.api.IEnergyContainerItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.Validate;

/**
 * Wrapper to make CoFH RF items look like Forge Energy holders.
 */
public class RFEnergyWrapper implements IEnergyStorage {
    private final ItemStack stack;
    private final IEnergyContainerItem container;

    public RFEnergyWrapper(ItemStack stack) {
        Validate.isTrue(stack.getItem() instanceof IEnergyContainerItem);
        this.stack = stack;
        container = (IEnergyContainerItem) stack.getItem();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return container.receiveEnergy(stack, maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return container.extractEnergy(stack, maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return container.getEnergyStored(stack);
    }

    @Override
    public int getMaxEnergyStored() {
        return container.getMaxEnergyStored(stack);
    }

    @Override
    public boolean canExtract() {
        return container.extractEnergy(stack, getEnergyStored(), true) > 0;
    }

    @Override
    public boolean canReceive() {
        return container.receiveEnergy(stack, getEnergyStored(), true) > 0;
    }
}
