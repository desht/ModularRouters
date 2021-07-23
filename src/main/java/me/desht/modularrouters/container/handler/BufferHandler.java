package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;

public class BufferHandler extends ItemStackHandler {
    private final ModularRouterBlockEntity router;
    private boolean hasFluidCap;
    private boolean hasEnergyCap;

    private final IFluidHandler adapter = new FluidItemAdapter(0);
    private final LazyOptional<IFluidHandler> fluidAdapter = LazyOptional.of(() -> adapter);

    public BufferHandler(ModularRouterBlockEntity router) {
        super(router.getBufferSlotCount());
        this.router = router;
        this.hasFluidCap = hasCap(getStackInSlot(0), FLUID_HANDLER_ITEM_CAPABILITY);
        this.hasEnergyCap = hasCap(getStackInSlot(0), CapabilityEnergy.ENERGY);
    }

    @Override
    public void onContentsChanged(int slot) {
        ItemStack stack = getStackInSlot(slot);

        boolean newFluidCap = hasCap(stack, FLUID_HANDLER_ITEM_CAPABILITY);
        boolean newEnergyCap = hasCap(stack, CapabilityEnergy.ENERGY);

        if (newFluidCap != hasFluidCap || newEnergyCap != hasEnergyCap) {
            // in case any pipes/cables need to connect/disconnect
            router.getLevel().updateNeighborsAt(router.getBlockPos(), ModBlocks.MODULAR_ROUTER.get());
        }
        hasFluidCap = newFluidCap;
        hasEnergyCap = newEnergyCap;

        router.setChanged();  // will also update comparator output
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);

        ItemStack stack = getStackInSlot(0);
        this.hasFluidCap = hasCap(stack, FLUID_HANDLER_ITEM_CAPABILITY);
        this.hasEnergyCap = hasCap(stack, CapabilityEnergy.ENERGY);
    }

    public LazyOptional<IFluidHandlerItem> getFluidItemCapability() {
        return getStackInSlot(0).getCapability(FLUID_HANDLER_ITEM_CAPABILITY);
    }

    public LazyOptional<IFluidHandler> getFluidCapability() {
        return hasFluidCap ? fluidAdapter : LazyOptional.empty();
    }

    public LazyOptional<IEnergyStorage> getEnergyCapability() {
        return getStackInSlot(0).getCapability(CapabilityEnergy.ENERGY);
    }

    private boolean hasCap(ItemStack stack, Capability<?> cap) {
        return stack.getCount() == 1 && stack.getCapability(cap).isPresent();
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
}
