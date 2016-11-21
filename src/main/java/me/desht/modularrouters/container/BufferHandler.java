package me.desht.modularrouters.container;

import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BufferHandler extends ItemStackHandler {
    private final TileEntityItemRouter router;
    private IFluidHandler fluidHandler;

    public BufferHandler(TileEntityItemRouter router) {
        super(1);
        this.router = router;
    }

    @Override
    public void onContentsChanged(int slot) {
        router.markDirty();  // will also update comparator output

        ItemStack stack = getStackInSlot(0);
        IFluidHandler newHandler = (stack != null && stack.stackSize == 1) ? FluidUtil.getFluidHandler(stack) : null;
        if (newHandler != fluidHandler) {
            fluidHandler = newHandler;
            router.getWorld().notifyNeighborsOfStateChange(router.getPos(), ModBlocks.itemRouter);
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);

        ItemStack stack = getStackInSlot(0);
        fluidHandler = (stack != null && stack.stackSize == 1) ? FluidUtil.getFluidHandler(stack) : null;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }
}
