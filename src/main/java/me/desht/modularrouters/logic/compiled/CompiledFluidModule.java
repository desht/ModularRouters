package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.FluidModule.FluidDirection;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class CompiledFluidModule extends CompiledModule {
    public static String NBT_MAX_TRANSFER = "MaxTransfer";
    public static String NBT_FLUID_DIRECTION = "FluidDir";

    private final int maxTransfer;
    private final FluidDirection fluidDirection;

    private static final InfiniteWaterHandler infiniteWater = new InfiniteWaterHandler();

    public CompiledFluidModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        NBTTagCompound compound = setupNBT(stack);
        maxTransfer = compound.getInteger(NBT_MAX_TRANSFER);
        fluidDirection = FluidDirection.values()[compound.getByte(NBT_FLUID_DIRECTION)];
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        ItemStack containerStack = router.getBufferItemStack();

        if (containerStack.getCount() != 1 || !getFilter().test(containerStack)) {
            return false;
        }

        IFluidHandlerItem routerFluidHandler = FluidUtil.getFluidHandler(containerStack);
        if (routerFluidHandler == null) {
            return false;
        }

        IFluidHandler worldFluidHandler;
        if (fluidDirection == FluidDirection.IN && isInfiniteWaterSource(router.getWorld(), getTarget().pos)) {
            // allows router to pull from infinite water source without block updates
            worldFluidHandler = infiniteWater;
        } else {
            worldFluidHandler = FluidUtil.getFluidHandler(router.getWorld(), getTarget().pos, getFacing().getOpposite());
        }

        if (worldFluidHandler == null) {
            // no fluid handler at the target blockpos - try to pour fluid out into the world?
            if (fluidDirection == FluidDirection.OUT && tryPourOutFluid(routerFluidHandler, router.getWorld(), getTarget().pos, containerStack)) {
                router.setBufferItemStack(routerFluidHandler.getContainer());
                return true;
            } else {
                return false;
            }
        }

        boolean transferDone;
        switch (fluidDirection) {
            case IN:
                transferDone = doTransfer(router, worldFluidHandler, routerFluidHandler, FluidDirection.IN);
                break;
            case OUT:
                transferDone = doTransfer(router, routerFluidHandler, worldFluidHandler, FluidDirection.OUT);
                break;
            default: return false;
        }
        if (transferDone) {
            router.setBufferItemStack(routerFluidHandler.getContainer());
        }
        return transferDone;
    }

    private boolean isInfiniteWaterSource(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);

        if ((state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER) && state.getValue(BlockLiquid.LEVEL) == 0) {
            int count = 0;
            for (EnumFacing face : EnumFacing.HORIZONTALS) {
                IBlockState state2 = world.getBlockState(pos.offset(face));
                if ((state2.getBlock() == Blocks.WATER || state2.getBlock() == Blocks.FLOWING_WATER) && state2.getValue(BlockLiquid.LEVEL) == 0) {
                    if (++count >= 2) return true;
                }
            }
        }
        return false;
    }

    private boolean tryPourOutFluid(IFluidHandler routerFluidHandler, World world, BlockPos pos, ItemStack containerStack) {
        FluidStack toPlace = routerFluidHandler.drain(1000, false);
        if (toPlace == null || toPlace.amount < 1000) {
            return false;  // must be a full bucket's worth to place in the world
        }
        FluidActionResult res = FluidUtil.tryPlaceFluid(null, world, pos, containerStack, toPlace);
        if (res.isSuccess()) {
            routerFluidHandler.drain(1000, true);
            // ensure that liquids start flowing (without this, water & lava stay static)
            world.neighborChanged(pos, world.getBlockState(pos).getBlock(), pos);
            return true;
        }
        return false;
    }

    private boolean doTransfer(TileEntityItemRouter router, IFluidHandler src, IFluidHandler dest, FluidDirection direction) {
        if (getRegulationAmount() > 0) {
            if (direction == FluidDirection.IN && checkFluidPercent(src) <= getRegulationAmount()) {
                return false;
            } else if (direction == FluidDirection.OUT && checkFluidPercent(dest) >= getRegulationAmount()) {
                return false;
            }
        }
        int amount = Math.min(maxTransfer, router.getCurrentFluidTransferAllowance(direction));
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

    private int checkFluidPercent(IFluidHandler handler) {
        // note: total amount of all fluids in all tanks... not ideal for inventories with multiple tanks
        int total = 0, max = 0;
        for (IFluidTankProperties tank : handler.getTankProperties()) {
            max += tank.getCapacity();
            if (tank.getContents() != null) total += tank.getContents().amount;
        }
        return (total * 100) / max;
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

    private static class InfiniteWaterHandler implements IFluidHandler {
        private static final IFluidTankProperties waterTank = new IFluidTankProperties() {
            @Nullable
            @Override
            public FluidStack getContents() {
                return new FluidStack(FluidRegistry.WATER, 1000);
            }

            @Override
            public int getCapacity() {
                return 1000;
            }

            @Override
            public boolean canFill() {
                return false;
            }

            @Override
            public boolean canDrain() {
                return true;
            }

            @Override
            public boolean canFillFluidType(FluidStack fluidStack) {
                return false;
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
                return fluidStack.getFluid() == FluidRegistry.WATER;
            }
        };

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[] { waterTank };
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return new FluidStack(FluidRegistry.WATER, resource.amount);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return new FluidStack(FluidRegistry.WATER, maxDrain);
        }
    }
}
