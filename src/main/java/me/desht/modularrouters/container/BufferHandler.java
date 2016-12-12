package me.desht.modularrouters.container;

import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.integration.tesla.TeslaIntegration;
import net.darkhax.tesla.lib.TeslaUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BufferHandler extends ItemStackHandler {
    private final TileEntityItemRouter router;
    private IFluidHandler fluidHandler;
    private boolean energyHandler;

    public BufferHandler(TileEntityItemRouter router) {
        super(1);
        this.router = router;
    }

    @Override
    public void onContentsChanged(int slot) {
        router.markDirty();  // will also update comparator output

        ItemStack stack = getStackInSlot(0);

        IFluidHandler newHandler = stack.getCount() == 1 ? FluidUtil.getFluidHandler(stack) : null;
        if (newHandler != fluidHandler) {
            boolean doUpdate = newHandler == null || fluidHandler == null;
            fluidHandler = newHandler;
            if (doUpdate) {
                // in case any fluid pipes need to connect/disconnect
                router.getWorld().notifyNeighborsOfStateChange(router.getPos(), ModBlocks.itemRouter, true);
            }
        }

        boolean newEnergyHandler = canHandleEnergy(stack);
        if (newEnergyHandler != energyHandler) {
            // in case any cables need to connect/disconnect
            energyHandler = newEnergyHandler;
            router.getWorld().notifyNeighborsOfStateChange(router.getPos(), ModBlocks.itemRouter, true);
        }
    }

    private boolean canHandleEnergy(ItemStack stack) {
        return (stack.hasCapability(CapabilityEnergy.ENERGY, null) ||
                (TeslaIntegration.enabled && TeslaUtils.hasTeslaSupport(stack, null)));
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);

        ItemStack stack = getStackInSlot(0);
        fluidHandler = stack.getCount() == 1 ? FluidUtil.getFluidHandler(stack) : null;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }
}
