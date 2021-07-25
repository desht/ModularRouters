package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.FluidModule1.FluidDirection;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
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
    public static final String NBT_REGULATE_ABSOLUTE = "RegulateAbsolute";

    private final int maxTransfer;
    private final FluidDirection fluidDirection;
    private final boolean forceEmpty;  // force emptying even if there's a fluid block in the way
    private final boolean regulateAbsolute;  // true = regulate by mB; false = regulate by % of tank's capacity

//    private static final InfiniteWaterHandler infiniteWater = new InfiniteWaterHandler();

    public CompiledFluidModule1(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        CompoundTag compound = setupNBT(stack);
        maxTransfer = compound.getInt(NBT_MAX_TRANSFER);
        fluidDirection = FluidDirection.values()[compound.getByte(NBT_FLUID_DIRECTION)];
        forceEmpty = compound.getBoolean(NBT_FORCE_EMPTY);
        regulateAbsolute = compound.getBoolean(NBT_REGULATE_ABSOLUTE);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        if (getTarget() == null) return false;

        LazyOptional<IFluidHandlerItem> routerCap = router.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

        if (!routerCap.isPresent()) {
            return false;
        }

        Level world = router.getLevel();
        BlockPos pos = getTarget().gPos.pos();
        LazyOptional<IFluidHandler> worldFluidCap = FluidUtil.getFluidHandler(world, pos, getFacing().getOpposite());

        boolean didWork = false;
        if (!worldFluidCap.isPresent()) {
            // no TE at the target position; try to interact with a fluid block in the world
            boolean playSound = router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) == 0;
            didWork = switch (fluidDirection) {
                case IN -> tryPickupFluid(routerCap, world, pos, playSound);
                case OUT -> tryPourOutFluid(routerCap, world, pos, playSound);
            };
        } else {
            // there's a TE with a fluid capability; try to interact with that
            didWork = switch (fluidDirection) {
                case IN -> worldFluidCap.map(srcHandler ->
                        routerCap.map(dstHandler -> doTransfer(router, srcHandler, dstHandler, FluidDirection.IN)).orElse(false))
                        .orElse(false);
                case OUT -> routerCap.map(srcHandler ->
                        worldFluidCap.map(dstHandler -> doTransfer(router, srcHandler, dstHandler, FluidDirection.OUT)).orElse(false))
                        .orElse(false);
            };
        }

        if (didWork) {
            routerCap.ifPresent(h -> router.setBufferItemStack(h.getContainer()));
        }
        return didWork;
    }

    private boolean tryPickupFluid(LazyOptional<IFluidHandlerItem> routerCap, Level world, BlockPos pos, boolean playSound) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup bucketPickup)) {
            return false;
        }

        // first check that the fluid matches any filter, and can be inserted
        FluidState fluidState = state.getFluidState();
        Fluid fluid = fluidState.getType();
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
        bucketPickup.pickupBlock(world, pos, state);
        FluidStack transferred = routerCap.map(h ->
                FluidUtil.tryFluidTransfer(h, tank, BUCKET_VOLUME, true))
                .orElse(FluidStack.EMPTY);
        if (!transferred.isEmpty() && playSound) {
            playFillSound(world, pos, fluid);
        }
        return !transferred.isEmpty();
    }

    private boolean tryPourOutFluid(LazyOptional<IFluidHandlerItem> routerFluidCap, Level world, BlockPos pos, boolean playSound) {
        if (!forceEmpty && !(world.isEmptyBlock(pos) || world.getBlockState(pos).getBlock() instanceof LiquidBlockContainer)) {
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
            if (world.isEmptyBlock(pos) || isNotSolid || isReplaceable || block instanceof LiquidBlockContainer && ((LiquidBlockContainer)block).canPlaceLiquid(world, pos, blockstate, toPlace.getFluid())) {
                if (world.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
                    // no pouring water in the nether!
                    playEvaporationEffects(world, pos);
                } else if (block instanceof LiquidBlockContainer liq) {
                    // a block which can take fluid, e.g. waterloggable block like a slab
                    FluidState still = fluid instanceof FlowingFluid ? ((FlowingFluid) fluid).getSource(false) : fluid.defaultFluidState();
                    if (liq.placeLiquid(world, pos, blockstate, still) && playSound) {
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
                    world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 3);
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

    private void playEmptySound(Level world, BlockPos pos, Fluid fluid) {
        SoundEvent soundevent = fluid.getAttributes().getEmptySound();
        if(soundevent == null) soundevent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        world.playSound(null, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void playFillSound(Level world, BlockPos pos, Fluid fluid) {
        SoundEvent soundEvent = fluid.getAttributes().getFillSound();
        if (soundEvent == null) soundEvent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
        world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void playEvaporationEffects(Level world, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
        for(int l = 0; l < 8; ++l) {
            world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
        }
    }

    private boolean isInfiniteWaterSource(Level world, BlockPos pos) {
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

    private boolean doTransfer(ModularRouterBlockEntity router, IFluidHandler src, IFluidHandler dest, FluidDirection direction) {
        if (getRegulationAmount() > 0) {
            if (direction == FluidDirection.IN && checkFluidInTank(src) <= getRegulationAmount()) {
                return false;
            } else if (direction == FluidDirection.OUT && checkFluidInTank(dest) >= getRegulationAmount()) {
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

    private int checkFluidInTank(IFluidHandler handler) {
        // note: total amount of all fluids in all tanks... not ideal for inventories with multiple tanks
        int total = 0, max = 0;
        if (isRegulateAbsolute()) {
            for (int idx = 0; idx < handler.getTanks(); idx++) {
                total += handler.getFluidInTank(idx).getAmount();
            }
            return total;
        } else {
            for (int idx = 0; idx < handler.getTanks(); idx++) {
                max += handler.getTankCapacity(idx);
                total += handler.getFluidInTank(idx).getAmount();
            }
            return max == 0 ? 0 : (total * 100) / max;
        }
    }

    private CompoundTag setupNBT(ItemStack stack) {
        CompoundTag compound = ModuleHelper.validateNBT(stack);
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

    public boolean isRegulateAbsolute() {
        return regulateAbsolute;
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
