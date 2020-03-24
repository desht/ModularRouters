package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class BufferHandler extends ItemStackHandler {
    private final TileEntityItemRouter router;
    private LazyOptional<IFluidHandlerItem> fluidCap = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.empty();
    private final IFluidHandler adapter = new FluidItemAdapter(0);
    private LazyOptional<IFluidHandler> fluidAdapter = LazyOptional.of(() -> adapter);

    public BufferHandler(TileEntityItemRouter router) {
        super(router.getBufferSlotCount());
        this.router = router;
    }

    @Override
    public void onContentsChanged(int slot) {
        router.markDirty();  // will also update comparator output

        ItemStack stack = getStackInSlot(slot);

        LazyOptional<IFluidHandlerItem> newFluidCap = stack.getCount() == 1 ? FluidUtil.getFluidHandler(stack) : LazyOptional.empty();
        boolean updateFluid = newFluidCap.isPresent() && !fluidCap.isPresent() || !newFluidCap.isPresent() && fluidCap.isPresent();
        fluidCap.invalidate();
        fluidAdapter.invalidate();
        fluidCap = newFluidCap;
        fluidAdapter = LazyOptional.of(() -> adapter);  // need to reassign to revalidate it after the invalidate() call

        LazyOptional<IEnergyStorage> newEnergyCap = stack.getCount() == 1 ? stack.getCapability(CapabilityEnergy.ENERGY) : LazyOptional.empty();
        boolean updateEnergy = newEnergyCap.isPresent() && !energyCap.isPresent() || !newEnergyCap.isPresent() && energyCap.isPresent();
        energyCap.invalidate();
        energyCap = newEnergyCap;

        if (updateFluid || updateEnergy) {
            // in case any pipes/cables need to connect/disconnect
            router.getWorld().notifyNeighborsOfStateChange(router.getPos(), ModBlocks.ITEM_ROUTER.get());
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);

        ItemStack stack = getStackInSlot(0);
        fluidCap = stack.getCount() == 1 ? FluidUtil.getFluidHandler(stack) : LazyOptional.empty();
        energyCap = stack.getCount() == 1 ? stack.getCapability(CapabilityEnergy.ENERGY) : LazyOptional.empty();
    }

    public LazyOptional<IFluidHandlerItem> getFluidItemCapability() {
        return fluidCap;
    }

    public LazyOptional<IFluidHandler> getFluidCapability() {
        return fluidCap.isPresent() ? fluidAdapter : LazyOptional.empty();
    }

    public LazyOptional<IEnergyStorage> getEnergyCapability() {
        return energyCap;
    }

    private class FluidItemAdapter implements IFluidHandler {
        private final int slot;

        public FluidItemAdapter(int slot) {
            this.slot = slot;
        }

        @Override
        public int getTanks() {
            return fluidCap.map(IFluidHandler::getTanks).orElse(0);
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return fluidCap.map(h -> h.getFluidInTank(tank)).orElse(FluidStack.EMPTY);
        }

        @Override
        public int getTankCapacity(int tank) {
            return fluidCap.map(h -> h.getTankCapacity(tank)).orElse(0);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return fluidCap.map(h -> h.isFluidValid(tank, stack)).orElse(false);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return fluidCap.map(h -> {
                int filled = h.fill(resource, action);
                if (action.execute()) setStackInSlot(slot, h.getContainer());
                return filled;
            }).orElse(0);
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return fluidCap.map(h -> {
                FluidStack drained = h.drain(resource, action);
                if (action.execute()) setStackInSlot(slot, h.getContainer());
                return drained;
            }).orElse(FluidStack.EMPTY);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return fluidCap.map(h -> {
                FluidStack drained = h.drain(maxDrain, action);
                if (action.execute()) setStackInSlot(slot, h.getContainer());
                return drained;
            }).orElse(FluidStack.EMPTY);
        }
    }
}
