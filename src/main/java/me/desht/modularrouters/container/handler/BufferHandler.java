package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemStackHandler;

public class BufferHandler extends ItemStackHandler {
    private final TileEntityItemRouter router;
    private LazyOptional<IFluidHandlerItem> fluidCap = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.empty();

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
        fluidCap = newFluidCap;

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

    public LazyOptional<IFluidHandlerItem> getFluidCapability() {
        return fluidCap;
    }

    public LazyOptional<IEnergyStorage> getEnergyCapability() {
        return energyCap;
    }
}
