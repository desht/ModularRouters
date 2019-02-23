package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.FluidModule.FluidDirection;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class CompiledFluidModule extends CompiledModule {
    public static final String NBT_FORCE_EMPTY = "ForceEmpty";
    public static final String NBT_MAX_TRANSFER = "MaxTransfer";
    public static final String NBT_FLUID_DIRECTION = "FluidDir";

    private final int maxTransfer;
    private final FluidDirection fluidDirection;
    private final boolean forceEmpty;  // force emptying even if there's a fluid block in the way

//    private static final InfiniteWaterHandler infiniteWater = new InfiniteWaterHandler();

    public CompiledFluidModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        NBTTagCompound compound = setupNBT(stack);
        maxTransfer = compound.getInt(NBT_MAX_TRANSFER);
        fluidDirection = FluidDirection.values()[compound.getByte(NBT_FLUID_DIRECTION)];
        forceEmpty = compound.getBoolean(NBT_FORCE_EMPTY);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        ItemStack containerStack = router.getBufferItemStack();
        World world = router.getWorld();
        LazyOptional<IFluidHandlerItem> routerCap = router.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

        if (!routerCap.isPresent()) {
            return false;
        }

        if (fluidDirection == FluidDirection.OUT
                && (!getFilter().test(containerStack) || !forceEmpty && !world.isAirBlock(getTarget().pos))) {
            return false;
        }

        LazyOptional<IFluidHandler> worldFluidCap;
        // @todo 1.13
//        if (fluidDirection == FluidDirection.IN && isInfiniteWaterSource(world, getTarget().pos)) {
//            // allows router to pull from infinite water source without block updates
//            worldFluidCap = LazyOptional.of(() -> infiniteWater);
//        } else {
//            worldFluidCap = FluidUtil.getFluidHandler(world, getTarget().pos, getFacing().getOpposite());
//        }
        worldFluidCap = FluidUtil.getFluidHandler(world, getTarget().pos, getFacing().getOpposite());

        if (worldFluidCap == null) {
            // no fluid handler at the target blockpos - try to pour fluid out into the world?
            if (fluidDirection == FluidDirection.OUT && tryPourOutFluid(routerCap, router.getWorld(), getTarget().pos, containerStack)) {
                routerCap.ifPresent(handler -> router.setBufferItemStack(handler.getContainer()));
                return true;
            } else {
                return false;
            }
        }

        if (fluidDirection == FluidDirection.IN) {
            boolean ok = worldFluidCap.map(handler -> {
                FluidStack fluidStack = handler.getTankProperties()[0].getContents();
                Fluid fluid = fluidStack == null ? null : fluidStack.getFluid();
                return getFilter().testFluid(fluid);
            }).orElse(false);
            if (!ok) return false;
        }

        boolean transferDone;
        switch (fluidDirection) {
            case IN:
                transferDone = worldFluidCap.map(srcHandler -> routerCap.map(dstHandler -> doTransfer(router, srcHandler, dstHandler, FluidDirection.IN)).orElse(false)).orElse(false);
                break;
            case OUT:
                transferDone = routerCap.map(srcHandler -> worldFluidCap.map(dstHandler -> doTransfer(router, srcHandler, dstHandler, FluidDirection.OUT)).orElse(false)).orElse(false);
                break;
            default: return false;
        }
        if (transferDone) {
            routerCap.ifPresent(handler -> router.setBufferItemStack(handler.getContainer()));
        }
        return transferDone;
    }

    private boolean isInfiniteWaterSource(World world, BlockPos pos) {
        // @todo 1.13
//        IBlockState state = world.getBlockState(pos);
//
//        if ((state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER) && state.getValue(BlockLiquid.LEVEL) == 0) {
//            int count = 0;
//            for (EnumFacing face : EnumFacing.HORIZONTALS) {
//                IBlockState state2 = world.getBlockState(pos.offset(face));
//                if ((state2.getBlock() == Blocks.WATER || state2.getBlock() == Blocks.FLOWING_WATER) && state2.getValue(BlockLiquid.LEVEL) == 0) {
//                    if (++count >= 2) return true;
//                }
//            }
//        }
        return false;
    }

    private boolean tryPourOutFluid(LazyOptional<IFluidHandlerItem> routerFluidCap, World world, BlockPos pos, ItemStack containerStack) {
        if (!forceEmpty && !world.isAirBlock(pos)) {
            return false;
        }
        return routerFluidCap.map(handler -> {
            FluidStack toPlace = handler.drain(1000, false);
            if (toPlace == null || toPlace.amount < 1000) {
                return false;  // must be a full bucket's worth to place in the world
            }
            FluidActionResult res = FluidUtil.tryPlaceFluid(null, world, pos, containerStack, toPlace);
            if (res.isSuccess()) {
                handler.drain(1000, true);
                // ensure that liquids start flowing (without this, water & lava stay static)
                world.neighborChanged(pos, world.getBlockState(pos).getBlock(), pos);
                return true;
            }
            return false;
        }).orElse(false);
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
        if (!compound.contains(NBT_MAX_TRANSFER)) {
            compound.putInt(NBT_MAX_TRANSFER, 1000);
        }
        if (!compound.contains(NBT_FLUID_DIRECTION)) {
            compound.putByte(NBT_FLUID_DIRECTION, (byte) FluidDirection.IN.ordinal());
        }
        return compound;
    }

    public FluidDirection getFluidDirection() {
        return fluidDirection;
    }

    public int getMaxTransfer() {
        return maxTransfer;
    }

    public boolean isForceEmpty() {
        return forceEmpty;
    }

    // @todo 1.13
//    private static class InfiniteWaterHandler implements IFluidHandler {
//        private static final IFluidTankProperties waterTank = new IFluidTankProperties() {
//            @Nullable
//            @Override
//            public FluidStack getContents() {
//                return new FluidStack(FluidRegistry.WATER, 1000);
//            }
//
//            @Override
//            public int getCapacity() {
//                return 1000;
//            }
//
//            @Override
//            public boolean canFill() {
//                return false;
//            }
//
//            @Override
//            public boolean canDrain() {
//                return true;
//            }
//
//            @Override
//            public boolean canFillFluidType(FluidStack fluidStack) {
//                return false;
//            }
//
//            @Override
//            public boolean canDrainFluidType(FluidStack fluidStack) {
//                return fluidStack.getFluid() == FluidRegistry.WATER;
//            }
//        };
//
//        @Override
//        public IFluidTankProperties[] getTankProperties() {
//            return new IFluidTankProperties[] { waterTank };
//        }
//
//        @Override
//        public int fill(FluidStack resource, boolean doFill) {
//            return 0;
//        }
//
//        @Nullable
//        @Override
//        public FluidStack drain(FluidStack resource, boolean doDrain) {
//            return new FluidStack(FluidRegistry.WATER, resource.amount);
//        }
//
//        @Nullable
//        @Override
//        public FluidStack drain(int maxDrain, boolean doDrain) {
//            return new FluidStack(FluidRegistry.WATER, maxDrain);
//        }
//    }
}
