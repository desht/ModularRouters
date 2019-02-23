package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ObjectRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

        LazyOptional<IFluidHandlerItem> newHandler = stack.getCount() == 1 ? FluidUtil.getFluidHandler(stack) : LazyOptional.empty();
        boolean updateFluid = newHandler.isPresent() && !fluidCap.isPresent() || !newHandler.isPresent() && fluidCap.isPresent();
        fluidCap = newHandler;

        LazyOptional<IEnergyStorage> newEnergyStorage = stack.getCount() == 1 ? stack.getCapability(CapabilityEnergy.ENERGY) : LazyOptional.empty();
        boolean updateEnergy = newEnergyStorage.isPresent() && !energyCap.isPresent() || !newEnergyStorage.isPresent() && energyCap.isPresent();
        energyCap = newEnergyStorage;

        if (updateFluid || updateEnergy) {
            // in case any pipes/cables need to connect/disconnect
            router.getWorld().notifyNeighborsOfStateChange(router.getPos(), ObjectRegistry.ITEM_ROUTER);
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
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
