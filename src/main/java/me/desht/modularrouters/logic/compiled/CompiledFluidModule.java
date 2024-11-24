package me.desht.modularrouters.logic.compiled;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.settings.TransferDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public class CompiledFluidModule extends CompiledModule {
    private final FluidModuleSettings settings;

    public CompiledFluidModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        settings = stack.getOrDefault(ModDataComponents.FLUID_SETTINGS.get(), FluidModuleSettings.DEFAULT);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        if (getTarget() == null) return false;

        IFluidHandlerItem routerHandler = router.getFluidHandler();
        if (routerHandler == null) return false;

        Level world = Objects.requireNonNull(router.getLevel());
        Optional<IFluidHandler> targetFluidHandler = getTarget().getFluidHandler();

        boolean didWork;
        if (targetFluidHandler.isPresent()) {
            // there's a block entity with a fluid capability; try to interact with that
            didWork = switch (getFluidDirection()) {
                case TO_ROUTER -> targetFluidHandler.map(worldHandler -> doTransfer(router, worldHandler, routerHandler, TransferDirection.TO_ROUTER))
                        .orElse(false);
                case FROM_ROUTER -> targetFluidHandler.map(worldHandler -> doTransfer(router, routerHandler, worldHandler, TransferDirection.FROM_ROUTER))
                        .orElse(false);
            };
        } else {
            // no block entity at the target position; try to interact with a fluid block in the world
            boolean playSound = router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) == 0;
            BlockPos pos = getTarget().gPos.pos();
            didWork = switch (getFluidDirection()) {
                case TO_ROUTER -> tryPickupFluid(router, routerHandler, world, pos, playSound);
                case FROM_ROUTER -> tryPourOutFluid(router, routerHandler, world, pos, playSound);
            };
        }

        if (didWork) {
            router.setBufferItemStack(routerHandler.getContainer());
        }
        return didWork;
    }

    private boolean tryPickupFluid(ModularRouterBlockEntity router, IFluidHandler routerHandler, Level world, BlockPos pos, boolean playSound) {
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
        FluidTank tank = new FluidTank(FluidType.BUCKET_VOLUME);
        tank.setFluid(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
        FluidStack maybeSent = FluidUtil.tryFluidTransfer(routerHandler, tank, FluidType.BUCKET_VOLUME, false);
        if (maybeSent.getAmount() != FluidType.BUCKET_VOLUME) {
            return false;
        }
        // actually do the pickup & transfer now
        bucketPickup.pickupBlock(router.getFakePlayer(), world, pos, state);
        FluidStack transferred = FluidUtil.tryFluidTransfer(routerHandler, tank, FluidType.BUCKET_VOLUME, true);
        if (!transferred.isEmpty() && playSound) {
            playFillSound(world, pos, fluid);
        }
        return !transferred.isEmpty();
    }

    private boolean tryPourOutFluid(ModularRouterBlockEntity router, IFluidHandler routerHandler, Level world, BlockPos pos, boolean playSound) {
        if (!isForceEmpty() && !(world.isEmptyBlock(pos) || world.getBlockState(pos).getBlock() instanceof LiquidBlockContainer)) {
            return false;
        }

        // code partially lifted from BucketItem

        FluidStack toPlace = routerHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
        if (toPlace.getAmount() < FluidType.BUCKET_VOLUME) {
            return false;  // must be a full bucket's worth to place in the world
        }
        Fluid fluid = toPlace.getFluid();
        if (!getFilter().testFluid(toPlace.getFluid())) {
            return false;
        }
        BlockState blockstate = world.getBlockState(pos);
        boolean isReplaceable = blockstate.canBeReplaced(fluid);
        Block block = blockstate.getBlock();
        if (world.isEmptyBlock(pos) || isReplaceable
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
                if (isReplaceable) {
                    world.destroyBlock(pos, true);
                }
                world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
            }
        }

        routerHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);

        return true;
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

    private boolean doTransfer(ModularRouterBlockEntity router, IFluidHandler src, IFluidHandler dest, TransferDirection direction) {
        if (getRegulationAmount() > 0) {
            if (direction == TransferDirection.TO_ROUTER && checkFluidInTank(src) <= getRegulationAmount()) {
                return false;
            } else if (direction == TransferDirection.FROM_ROUTER && checkFluidInTank(dest) >= getRegulationAmount()) {
                return false;
            }
        }
        int amount = Math.min(getMaxTransfer(), router.getCurrentFluidTransferAllowance(direction));
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

    public TransferDirection getFluidDirection() {
        return settings.direction;
    }

    public int getMaxTransfer() {
        return settings.maxTransfer == 0 ? FluidType.BUCKET_VOLUME : settings.maxTransfer;
    }

    public boolean isForceEmpty() {
        return settings.forceEmpty;
    }

    public boolean isRegulateAbsolute() {
        return settings.regulateAbsolute;
    }

    public record FluidModuleSettings(int maxTransfer, TransferDirection direction, boolean forceEmpty, boolean regulateAbsolute) {
        public static final FluidModuleSettings DEFAULT = new FluidModuleSettings(0, TransferDirection.TO_ROUTER, false, false);

        public static final Codec<FluidModuleSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                ExtraCodecs.NON_NEGATIVE_INT
                        .optionalFieldOf("max_transfer", 0)
                        .forGetter(FluidModuleSettings::maxTransfer),
                StringRepresentable.fromEnum(TransferDirection::values)
                        .optionalFieldOf("direction", TransferDirection.TO_ROUTER)
                        .forGetter(FluidModuleSettings::direction),
                Codec.BOOL
                        .optionalFieldOf("force_empty", false)
                        .forGetter(FluidModuleSettings::forceEmpty),
                Codec.BOOL
                        .optionalFieldOf("regulate_absolute", false)
                        .forGetter(FluidModuleSettings::regulateAbsolute)
        ).apply(builder, FluidModuleSettings::new));

        public static StreamCodec<FriendlyByteBuf, CompiledFluidModule.FluidModuleSettings> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, FluidModuleSettings::maxTransfer,
                NeoForgeStreamCodecs.enumCodec(TransferDirection.class), FluidModuleSettings::direction,
                ByteBufCodecs.BOOL, FluidModuleSettings::forceEmpty,
                ByteBufCodecs.BOOL, FluidModuleSettings::regulateAbsolute,
                CompiledFluidModule.FluidModuleSettings::new
        );
    }

}
