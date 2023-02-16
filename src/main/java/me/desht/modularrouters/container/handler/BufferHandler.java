package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

import static me.desht.modularrouters.core.ModItems.GAS_HANDLER_ITEM;

public class BufferHandler extends ItemStackHandler {
    private final ModularRouterBlockEntity router;
    private boolean hasFluidCap;
    private boolean hasGasCap;
    private boolean hasEnergyCap;

    private final IFluidHandler adapter = new FluidItemAdapter(0);

    private final IGasHandler adapter_gas = new GasItemAdapter(1);
    private final LazyOptional<IFluidHandler> fluidAdapter = LazyOptional.of(() -> adapter);

    private final LazyOptional<IGasHandler> gasAdapter = LazyOptional.of(() -> adapter_gas);


    public BufferHandler(ModularRouterBlockEntity router) {
        super(router.getBufferSlotCount());
        this.router = router;
        this.hasFluidCap = hasCap(getStackInSlot(0), ForgeCapabilities.FLUID_HANDLER_ITEM);
        this.hasGasCap = hasCap(getStackInSlot(0), GAS_HANDLER_ITEM);
        this.hasEnergyCap = hasCap(getStackInSlot(0), ForgeCapabilities.ENERGY);
    }

    @Override
    public void onContentsChanged(int slot) {
        ItemStack stack = getStackInSlot(slot);

        boolean newFluidCap = hasCap(stack, ForgeCapabilities.FLUID_HANDLER_ITEM);
        boolean newGasCap = hasCap(stack, GAS_HANDLER_ITEM);
        boolean newEnergyCap = hasCap(stack, ForgeCapabilities.ENERGY);

        if (newFluidCap != hasFluidCap || newEnergyCap != hasEnergyCap) {
            // in case any pipes/cables need to connect/disconnect
            router.nonNullLevel().updateNeighborsAt(router.getBlockPos(), ModBlocks.MODULAR_ROUTER.get());
        }
        if (newGasCap != hasGasCap || newEnergyCap != hasEnergyCap) {
            // in case any pipes/cables need to connect/disconnect
            router.nonNullLevel().updateNeighborsAt(router.getBlockPos(), ModBlocks.MODULAR_ROUTER.get());
        }

        hasFluidCap = newFluidCap;
        hasGasCap = newGasCap;
        hasEnergyCap = newEnergyCap;

        router.setChanged();  // will also update comparator output
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);

        ItemStack stack = getStackInSlot(0);
        this.hasFluidCap = hasCap(stack, ForgeCapabilities.FLUID_HANDLER_ITEM);
        this.hasGasCap = hasCap(stack, GAS_HANDLER_ITEM);
        this.hasEnergyCap = hasCap(stack, ForgeCapabilities.ENERGY);
    }

    public LazyOptional<IFluidHandlerItem> getFluidItemCapability() {
        ItemStack stack = getStackInSlot(0);
        return stack.getCount() == 1 ? getStackInSlot(0).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM) : LazyOptional.empty();
    }

    public LazyOptional<ModItems.IGasHandlerItem> getGasItemCapability() {
        ItemStack stack = getStackInSlot(0);
        return stack.getCount() == 1 ? getStackInSlot(0).getCapability(GAS_HANDLER_ITEM) : LazyOptional.empty();
    }

    public LazyOptional<IFluidHandler> getFluidCapability() {
        return hasFluidCap ? fluidAdapter : LazyOptional.empty();
    }

    public LazyOptional<IGasHandler> getGasCapability() {
        return hasGasCap ? gasAdapter : LazyOptional.empty();
    }

    public LazyOptional<IEnergyStorage> getEnergyCapability() {
        return getStackInSlot(0).getCapability(ForgeCapabilities.ENERGY);
    }

    private boolean hasCap(ItemStack stack, Capability<?> cap) {
        return stack.getCount() == 1 && stack.getCapability(cap).isPresent();
    }

    public void invalidateCaps() {
        getFluidCapability().invalidate();
        getGasCapability().invalidate();
        getEnergyCapability().invalidate();
    }

    private class FluidItemAdapter implements IFluidHandler {
        private final int slot;

        FluidItemAdapter(int slot) {
            this.slot = slot;
        }

        @Override
        public int getTanks() {
            return getFluidItemCapability().map(IFluidHandler::getTanks).orElse(0);
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return getFluidItemCapability().map(h -> h.getFluidInTank(tank)).orElse(FluidStack.EMPTY);
        }

        @Override
        public int getTankCapacity(int tank) {
            return getFluidItemCapability().map(h -> h.getTankCapacity(tank)).orElse(0);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return getFluidItemCapability().map(h -> h.isFluidValid(tank, stack)).orElse(false);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return getFluidItemCapability().map(h -> {
                int filled = h.fill(resource, action);
                if (action.execute() && filled != 0) setStackInSlot(slot, h.getContainer());
                return filled;
            }).orElse(0);
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return getFluidItemCapability().map(h -> {
                FluidStack drained = h.drain(resource, action);
                if (action.execute() && !drained.isEmpty()) setStackInSlot(slot, h.getContainer());
                return drained;
            }).orElse(FluidStack.EMPTY);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return getFluidItemCapability().map(h -> {
                FluidStack drained = h.drain(maxDrain, action);
                if (action.execute() && !drained.isEmpty()) setStackInSlot(slot, h.getContainer());
                return drained;
            }).orElse(FluidStack.EMPTY);
        }
    }


    private class GasItemAdapter implements IGasHandler {

        private final int slot;

        GasItemAdapter(int slot) {
            this.slot = slot;
        }

        @Override
        public int getTanks() {
            return getGasItemCapability().map(IGasHandler::getTanks).orElse(0);
        }


        @Nonnull
        @Override
        public GasStack getChemicalInTank(int tank) {
            return getGasItemCapability().map(h -> h.getChemicalInTank(tank)).orElse(GasStack.EMPTY);
        }

        @Override
        public void setChemicalInTank(int i, GasStack stack) {

        }

        @Override
        public long getTankCapacity(int var1) {
            return getGasItemCapability().map(h -> h.getTankCapacity(var1)).orElse(0L);
        }

        @Override
        public boolean isValid(int tank, GasStack stack) {
            return getGasItemCapability().map(h -> h.isValid(tank, stack)).orElse(false);
        }

        @Override
        public GasStack insertChemical(int i, GasStack stack, Action action) {
            return getGasItemCapability().map(h -> {
                GasStack inserted = h.insertChemical(i, stack, action);
                if (action.execute() && inserted.getAmount() != 0) {
                    setStackInSlot(slot, h.getContainer());
                }
                return inserted;
            }).orElse(GasStack.EMPTY);
        }

        @Nonnull
        @Override
        public GasStack extractChemical(GasStack resource, Action action) {
            return getGasItemCapability().map(h -> {
                GasStack extracted = h.extractChemical(resource, action);
                if (action.execute() && !extracted.isEmpty()) {
                    setStackInSlot(slot, h.getContainer());
                }
                return extracted;
            }).orElse(GasStack.EMPTY);
        }
        @Override
        public GasStack extractChemical(long amount, Action action) {
            return getGasItemCapability().map(h -> {
                GasStack extracted = h.extractChemical(amount, action);
                if (action.execute() && !extracted.isEmpty()) {
                    setStackInSlot(slot, h.getContainer());
                }
                return extracted;
            }).orElse(GasStack.EMPTY);
        }

        @Override
        public GasStack extractChemical(int i, long l, Action action) {
            return getGasItemCapability().map(h -> {
                GasStack extracted = h.extractChemical(i, l, action);
                if (action.execute() && extracted.getAmount() != 0) {
                    setStackInSlot(slot, h.getContainer());
                }
                return extracted;
            }).orElse(GasStack.EMPTY);
        }

    }
}




