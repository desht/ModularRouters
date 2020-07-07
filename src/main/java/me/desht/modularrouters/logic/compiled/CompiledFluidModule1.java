package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.FluidModule1.FluidDirection;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;

import static net.minecraftforge.fluids.FluidAttributes.BUCKET_VOLUME;

public class CompiledFluidModule1 extends CompiledModule {
    public static final String NBT_FORCE_EMPTY = "ForceEmpty";
    public static final String NBT_MAX_TRANSFER = "MaxTransfer";
    public static final String NBT_FLUID_DIRECTION = "FluidDir";

    private final int maxTransfer;
    private final FluidDirection fluidDirection;
    private final boolean forceEmpty;  // force emptying even if there's a fluid block in the way

//    private static final InfiniteWaterHandler infiniteWater = new InfiniteWaterHandler();

    public CompiledFluidModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        CompoundNBT compound = setupNBT(stack);
        maxTransfer = compound.getInt(NBT_MAX_TRANSFER);
        fluidDirection = FluidDirection.values()[compound.getByte(NBT_FLUID_DIRECTION)];
        forceEmpty = compound.getBoolean(NBT_FORCE_EMPTY);
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        LazyOptional<IFluidHandlerItem> routerCap = router.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

        if (!routerCap.isPresent()) {
            return false;
        }

        World world = router.getWorld();
        BlockPos pos = getTarget().gPos.getPos();
        LazyOptional<IFluidHandler> worldFluidCap = FluidUtil.getFluidHandler(world, pos, getFacing().getOpposite());

        boolean didWork = false;
        if (!worldFluidCap.isPresent()) {
            // no TE at the target position; try to interact with a fluid block in the world
            boolean playSound = router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) == 0;
            switch (fluidDirection) {
                case IN:
                    didWork = tryPickupFluid(routerCap, world, pos, playSound);
                    break;
                case OUT:
                    didWork = tryPourOutFluid(routerCap, world, pos, playSound);
                    break;
            }
        } else {
            // there's a TE with a fluid capability; try to interact with that
            switch (fluidDirection) {
                case IN:
                    didWork = worldFluidCap.map(srcHandler ->
                            routerCap.map(dstHandler -> doTransfer(router, srcHandler, dstHandler, FluidDirection.IN)).orElse(false))
                            .orElse(false);
                    break;
                case OUT:
                    didWork = routerCap.map(srcHandler ->
                            worldFluidCap.map(dstHandler -> doTransfer(router, srcHandler, dstHandler, FluidDirection.OUT)).orElse(false))
                            .orElse(false);
                    break;
            }
        }

        if (didWork) {
            routerCap.ifPresent(h -> router.setBufferItemStack(h.getContainer()));
        }
        return didWork;
    }

    private boolean tryPickupFluid(LazyOptional<IFluidHandlerItem> routerCap, World world, BlockPos pos, boolean playSound) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof IBucketPickupHandler)) {
            return false;
        }

        // first check that the fluid matches any filter, and can be inserted
        FluidState fluidState = state.getFluidState();
        Fluid fluid = fluidState.getFluid();
        if (fluid == Fluids.EMPTY || !fluid.isSource(fluidState) || !getFilter().testFluid(fluid)) {
            return false;
        }
        FluidTank tank = new FluidTank(BUCKET_VOLUME);
        tank.setFluid(new FluidStack(fluid, BUCKET_VOLUME));
        FluidStack maybeSent = routerCap.map(
                h -> FluidUtil.tryFluidTransfer(h, tank, BUCKET_VOLUME, false)
        ).orElse(FluidStack.EMPTY);
        if (maybeSent.getAmount() != BUCKET_VOLUME) {
            return false;
        }
        // actually do the pickup & transfer now
        ((IBucketPickupHandler) state.getBlock()).pickupFluid(world, pos, state);
        FluidStack transferred = routerCap.map(h ->
                FluidUtil.tryFluidTransfer(h, tank, BUCKET_VOLUME, true))
                .orElse(FluidStack.EMPTY);
        if (!transferred.isEmpty() && playSound) {
            playFillSound(world, pos, fluid);
        }
        return !transferred.isEmpty();
    }

    private boolean tryPourOutFluid(LazyOptional<IFluidHandlerItem> routerFluidCap, World world, BlockPos pos, boolean playSound) {
        if (!forceEmpty && !(world.isAirBlock(pos) || world.getBlockState(pos).getBlock() instanceof ILiquidContainer)) {
            return false;
        }

        // code partially lifted from BucketItem
        BlockState blockstate = world.getBlockState(pos);
        Material material = blockstate.getMaterial();
        boolean isNotSolid = !material.isSolid();
        boolean isReplaceable = material.isReplaceable();

        boolean didWork = routerFluidCap.map(handler -> {
            FluidStack toPlace = handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
            if (toPlace.getAmount() < BUCKET_VOLUME) {
                return false;  // must be a full bucket's worth to place in the world
            }
            Fluid fluid = toPlace.getFluid();
            if (!getFilter().testFluid(toPlace.getFluid())) {
                return false;
            }
            Block block = blockstate.getBlock();
            if (world.isAirBlock(pos) || isNotSolid || isReplaceable || block instanceof ILiquidContainer && ((ILiquidContainer)block).canContainFluid(world, pos, blockstate, toPlace.getFluid())) {
                if (world.func_230315_m_().func_236040_e_() && fluid.isIn(FluidTags.WATER)) {
                    // no pouring water in the nether!
                    playEvaporationEffects(world, pos);
                } else if (block instanceof ILiquidContainer) {
                    // a block which can take fluid, e.g. waterloggable block like a slab
                    FluidState still = fluid instanceof FlowingFluid ? ((FlowingFluid) fluid).getStillFluidState(false) : fluid.getDefaultState();
                    if (((ILiquidContainer)block).receiveFluid(world, pos, blockstate, still) && playSound) {
                        playEmptySound(world, pos, fluid);
                    }
                } else {
                    // air or some non-solid/replaceable block: just overwrite with the fluid
                    if (playSound) {
                        playEmptySound(world, pos, fluid);
                    }
                    if (isNotSolid || isReplaceable) {
                        world.destroyBlock(pos, true);
                    }
                    world.setBlockState(pos, fluid.getDefaultState().getBlockState(), 3);
                }
                return true;
            }
            return false;
        }).orElse(false);

        if (didWork) {
            routerFluidCap.ifPresent(handler -> handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE));
        }
        return didWork;
    }

    private void playEmptySound(World world, BlockPos pos, Fluid fluid) {
        SoundEvent soundevent = fluid.getAttributes().getEmptySound();
        if(soundevent == null) soundevent = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
        world.playSound(null, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private void playFillSound(World world, BlockPos pos, Fluid fluid) {
        SoundEvent soundEvent = fluid.getAttributes().getFillSound();
        if (soundEvent == null) soundEvent = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private void playEvaporationEffects(World world, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
        for(int l = 0; l < 8; ++l) {
            world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
        }
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
        if (!newStack.isEmpty() && getFilter().testFluid(newStack.getFluid())) {
            newStack = FluidUtil.tryFluidTransfer(dest, src, newStack.getAmount(), true);
            if (!newStack.isEmpty()) {
                router.transferredFluid(newStack.getAmount(), direction);
                return true;
            }
        }
        return false;
    }

    private int checkFluidPercent(IFluidHandler handler) {
        // note: total amount of all fluids in all tanks... not ideal for inventories with multiple tanks
        int total = 0, max = 0;
        for (int idx = 0; idx < handler.getTanks(); idx++) {
            max += handler.getTankCapacity(idx);
            total += handler.getFluidInTank(idx).getAmount();
        }
        return (total * 100) / max;
    }

    private CompoundNBT setupNBT(ItemStack stack) {
        CompoundNBT compound = ModuleHelper.validateNBT(stack);
        if (!compound.contains(NBT_MAX_TRANSFER)) {
            compound.putInt(NBT_MAX_TRANSFER, BUCKET_VOLUME);
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
