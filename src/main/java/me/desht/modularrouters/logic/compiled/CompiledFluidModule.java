package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.FluidModule;
import me.desht.modularrouters.item.module.FluidModule.FluidDirection;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class CompiledFluidModule extends CompiledModule {
    public static String NBT_MAX_TRANSFER = "MaxTransfer";
    public static String NBT_FLUID_DIRECTION = "FluidDir";

    private final int maxTransfer;
    private final FluidDirection fluidDirection;

    public CompiledFluidModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        NBTTagCompound compound = setupNBT(stack);
        maxTransfer = compound.getInteger(NBT_MAX_TRANSFER);
        fluidDirection = FluidDirection.values()[compound.getByte(NBT_FLUID_DIRECTION)];
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        ItemStack stack = router.getBufferItemStack();

        if (getDirection() == Module.RelativeDirection.NONE || stack == null || stack.stackSize != 1) {
            return false;
        }

        IFluidHandler routerFluidHandler = FluidUtil.getFluidHandler(stack);
        if (routerFluidHandler == null) {
            return false;
        }
        IFluidHandler worldFluidHandler = FluidUtil.getFluidHandler(router.getWorld(), getTarget().pos, getFacing().getOpposite());
        if (worldFluidHandler == null) {
            // special case: try to pour fluid out into the world?
            return fluidDirection == FluidDirection.OUT && tryPourOutFluid(routerFluidHandler, router.getWorld(), getTarget().pos);
        }

        switch (fluidDirection) {
            case IN: return doTransfer(router, worldFluidHandler, routerFluidHandler, FluidDirection.IN);
            case OUT: return doTransfer(router, routerFluidHandler, worldFluidHandler, FluidDirection.OUT);
            default: return false;
        }
    }

    private boolean tryPourOutFluid(IFluidHandler routerFluidHandler, World world, BlockPos pos) {
        FluidStack toPlace = routerFluidHandler.drain(1000, false);
        if (toPlace == null || toPlace.amount < 1000) {
            return false;  // must be a full bucket's worth to place in the world
        }
        if (FluidUtil.tryPlaceFluid(null, world, toPlace, pos)) {
            routerFluidHandler.drain(1000, true);
            // ensure that liquids start flowing (without this, water & lava stay static)
            world.notifyBlockOfStateChange(pos, world.getBlockState(pos).getBlock());
            return true;
        }
        return false;
    }

    private boolean doTransfer(TileEntityItemRouter router, IFluidHandler src, IFluidHandler dest, FluidDirection direction) {
        int amount = Math.min(maxTransfer, router.getRemainingFluidTransferAllowance(direction));
        FluidStack newStack = FluidUtil.tryFluidTransfer(dest, src, amount, false);
        if (newStack != null && newStack.amount > 0) {
            newStack = FluidUtil.tryFluidTransfer(dest, src, newStack.amount, true);
            if (newStack != null && newStack.amount > 0) {
                router.transferredFluid(newStack.amount, direction);
                return true;
            }
        }
        return false;
    }

    private NBTTagCompound setupNBT(ItemStack stack) {
        NBTTagCompound compound = ModuleHelper.validateNBT(stack);
        if (!compound.hasKey(NBT_MAX_TRANSFER)) {
            compound.setInteger(NBT_MAX_TRANSFER, 1000);
        }
        if (!compound.hasKey(NBT_FLUID_DIRECTION)) {
            compound.setByte(NBT_FLUID_DIRECTION, (byte) FluidDirection.IN.ordinal());
        }
        return compound;
    }

    public FluidDirection getFluidDirection() {
        return fluidDirection;
    }

    public int getMaxTransfer() {
        return maxTransfer;
    }
}
