package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.FluidModule1.FluidDirection;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CompiledFluidModule1 extends CompiledModule {
    public static final String NBT_FORCE_EMPTY = "ForceEmpty";
    public static final String NBT_MAX_TRANSFER = "MaxTransfer";
    public static final String NBT_FLUID_DIRECTION = "FluidDir";
    public static final String NBT_REGULATE_ABSOLUTE = "RegulateAbsolute";

    public static final int BUCKET_VOLUME = 1000;

    private final int maxTransfer;
    private final FluidDirection fluidDirection;
    private final boolean forceEmpty;  // force emptying even if there's a fluid block in the way
    private final boolean regulateAbsolute;  // true = regulate by mB; false = regulate by % of tank's capacity

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

        LazyOptional<IFluidHandlerItem> routerCap = router.getCapability(Capabilities.FLUID_HANDLER_ITEM);

        if (!routerCap.isPresent()) {
            return false;
        }

        Level world = Objects.requireNonNull(router.getLevel());
        BlockPos pos = getTarget().gPos.pos();
        LazyOptional<IFluidHandler> worldFluidCap = FluidUtil.getFluidHandler(world, pos, getFacing().getOpposite());

        boolean didWork;
        if (worldFluidCap.isPresent()) {
            // there's a block entity with a fluid capability; try to interact with that
            didWork = switch (fluidDirection) {
                case IN -> worldFluidCap.map(srcHandler ->
                        routerCap.map(dstHandler -> doTransfer(router, srcHandler, dstHandler, FluidDirection.IN)).orElse(false))
                        .orElse(false);
                case OUT -> routerCap.map(srcHandler ->
                        worldFluidCap.map(dstHandler -> doTransfer(router, srcHandler, dstHandler, FluidDirection.OUT)).orElse(false))
                        .orElse(false);
            };
        } else {
            // no block entity at the target position; try to interact with a fluid block in the world
            boolean playSound = router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) == 0;
            didWork = switch (fluidDirection) {
                case IN -> tryPickupFluid(router, routerCap, world, pos, playSound);
                case OUT -> tryPourOutFluid(router, routerCap, world, pos, playSound);
            };
        }

        if (didWork) {
            routerCap.ifPresent(h -> router.setBufferItemStack(h.getContainer()));
        }
        return didWork;
    }

    private boolean tryPickupFluid(ModularRouterBlockEntity router, LazyOptional<IFluidHandlerItem> routerCap, Level world, BlockPos pos, boolean playSound) {
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
        bucketPickup.pickupBlock(router.getFakePlayer(), world, pos, state);
        FluidStack transferred = routerCap.map(destHandler ->
                FluidUtil.tryFluidTransfer(destHandler, tank, BUCKET_VOLUME, true))
                .orElse(FluidStack.EMPTY);
        if (!transferred.isEmpty() && playSound) {
            playFillSound(world, pos, fluid);
        }
        return !transferred.isEmpty();
    }

    private boolean tryPourOutFluid(ModularRouterBlockEntity router, LazyOptional<IFluidHandlerItem> routerFluidCap, Level world, BlockPos pos, boolean playSound) {
        if (!forceEmpty && !(world.isEmptyBlock(pos) || world.getBlockState(pos).getBlock() instanceof LiquidBlockContainer)) {
            return false;
        }

        // code partially lifted from BucketItem
//        Material material = blockstate.getMaterial();
//        boolean isNotSolid = !material.isSolid();

        boolean didWork = routerFluidCap.map(handler -> {
            FluidStack toPlace = handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
            if (toPlace.getAmount() < BUCKET_VOLUME) {
                return false;  // must be a full bucket's worth to place in the world
            }
            Fluid fluid = toPlace.getFluid();
            if (!getFilter().testFluid(toPlace.getFluid())) {
                return false;
            }
            BlockState blockstate = world.getBlockState(pos);
            boolean isReplaceable = blockstate.canBeReplaced(fluid);
            Block block = blockstate.getBlock();
            if (world.isEmptyBlock(pos) /*|| isNotSolid*/ || isReplaceable
                    || block instanceof LiquidBlockContainer liq && liq.canPlaceLiquid(router.getFakePlayer(), world, pos, blockstate, toPlace.getFluid())) {
                if (world.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
                    // no pouring water in the nether!
                    playEvaporationEffects(world, pos, fluid);
                } else if (block instanceof LiquidBlockContainer liq) {
                    // a block which can take fluid, e.g. waterloggable block like a slab
                    FluidState still = fluid instanceof FlowingFluid ff ? ff.getSource(false) : fluid.defaultFluidState();
                    if (liq.placeLiquid(world, pos, blockstate, still) && playSound) {
                        playEmptySound(world, pos, fluid);
                    }
                } else {
                    // air or some non-solid/replaceable block: just overwrite with the fluid
                    if (playSound) {
                        playEmptySound(world, pos, fluid);
                    }
                    if (/*isNotSolid ||*/ isReplaceable) {
                        world.destroyBlock(pos, true);
                    }
                    world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
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
        SoundEvent soundevent = fluid.getFluidType().getSound(SoundActions.BUCKET_EMPTY);
        if (soundevent != null) {
            world.playSound(null, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private void playFillSound(Level world, BlockPos pos, Fluid fluid) {
        SoundEvent soundEvent = fluid.getFluidType().getSound(SoundActions.BUCKET_FILL);
        if (soundEvent != null) {
            world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private void playEvaporationEffects(Level world, BlockPos pos, Fluid fluid) {
        SoundEvent soundEvent = fluid.getFluidType().getSound(SoundActions.FLUID_VAPORIZE);
        if (soundEvent != null) {
            world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        for (int l = 0; l < 8; ++l) {
            world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
        }
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
}
