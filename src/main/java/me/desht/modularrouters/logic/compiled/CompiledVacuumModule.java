package me.desht.modularrouters.logic.compiled;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.integration.XPCollection.XPCollectionType;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CompiledVacuumModule extends CompiledModule {
    public static final String NBT_XP_FLUID_TYPE = "XPFluidType";
    public static final String NBT_AUTO_EJECT = "AutoEject";

    private final VacuumSettings settings;
    private final boolean fastPickup;
    private final boolean xpMode;
    private final FluidStack xpJuiceStack;

    private BlockCapabilityCache<IFluidHandler,Direction> fluidReceiverCache = null;

    // temporary small xp buffer (generally around an orb or less)
    // does not survive router recompilation...
    private int xpBuffered = 0;

    public CompiledVacuumModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        settings = stack.getOrDefault(ModDataComponents.VACUUM_SETTINGS, VacuumSettings.DEFAULT);
        fastPickup = getAugmentCount(ModItems.FAST_PICKUP_AUGMENT) > 0;
        xpMode = getAugmentCount(ModItems.XP_VACUUM_AUGMENT) > 0;

        if (xpMode) {
            Fluid xpFluid = settings.collectionType.getFluid();
            xpJuiceStack = xpFluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(xpFluid, 1000);
        } else {
            xpJuiceStack = FluidStack.EMPTY;
        }
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        if (xpMode) {
            return handleXpMode(router);
        } else {
            return handleItemMode(router);
        }
    }

    @Override
    public void onNeighbourChange(ModularRouterBlockEntity router) {
        fluidReceiverCache = null;
    }

    @Override
    public List<ModuleTarget> setupTargets(ModularRouterBlockEntity router, ItemStack stack) {
        if (router == null) {
            return null;
        }
        RelativeDirection dir = getDirection();
        int offset = dir == RelativeDirection.NONE ? 0 : getRange() + 1;
        Direction facing = router.getAbsoluteFacing(dir);
        GlobalPos gPos = MiscUtil.makeGlobalPos(router.nonNullLevel(), router.getBlockPos().relative(facing, offset));
        return List.of(new ModuleTarget(gPos, facing));
    }

    @Override // the target is computed programmatically, their validity is not relevant
    protected boolean isTargetValid(ModularRouterBlockEntity router, ModuleTarget target) {
        return true;
    }

    private boolean handleItemMode(ModularRouterBlockEntity router) {
        if (router.isBufferFull()) {
            return false;
        }

        ItemStack bufferStack = router.getBuffer().getStackInSlot(0);

        BlockPos centrePos = getTarget().gPos.pos();
        int range = getRange();
        List<ItemEntity> items = router.nonNullLevel().getEntitiesOfClass(ItemEntity.class, new AABB(centrePos).inflate(range));

        int toPickUp = getItemsPerTick(router);

        for (ItemEntity item : items) {
            if (!item.isAlive() || (!fastPickup && item.hasPickUpDelay())) {
                continue;
            }
            ItemStack stackOnGround = item.getItem();
            if ((bufferStack.isEmpty() || ItemStack.isSameItemSameComponents(stackOnGround, bufferStack)) && getFilter().test(stackOnGround)) {
                int inRouter = bufferStack.getCount();
                int spaceInRouter = getRegulationAmount() > 0 ?
                        Math.min(stackOnGround.getMaxStackSize(), getRegulationAmount()) - inRouter :
                        stackOnGround.getMaxStackSize() - inRouter;
                ItemStack vacuumed = stackOnGround.split(Math.min(getItemsPerTick(router), spaceInRouter));
                ItemStack excess = router.insertBuffer(vacuumed);
                int remaining = excess == null ? 0 : excess.getCount();
                stackOnGround.grow(remaining);
                int inserted = vacuumed.getCount() - remaining;
                toPickUp -= inserted;
                if (stackOnGround.isEmpty()) {
                    item.remove(Entity.RemovalReason.DISCARDED);
                }
                if (inserted > 0 && ConfigHolder.common.module.vacuumParticles.get() && router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
                    ((ServerLevel) router.nonNullLevel()).sendParticles(ParticleTypes.CLOUD, item.getX(), item.getY() + 0.25, item.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                }
                if (toPickUp <= 0) {
                    break;
                }
            }
        }
        return toPickUp < getItemsPerTick(router);
    }

    private boolean handleXpMode(ModularRouterBlockEntity router) {
        int spaceForXp;
        IFluidHandler fluidHandler = null;

        if (getXPCollectionType().isSolid()) {
            ItemStack inRouterStack = router.getBufferItemStack();
            if (!inRouterStack.isEmpty() && !ItemStack.isSameItemSameComponents(inRouterStack, getXPCollectionType().getIcon())) {
                return false;
            }
            spaceForXp = ((inRouterStack.isEmpty() ? getXPCollectionType().getIcon() : inRouterStack).getMaxStackSize() - inRouterStack.getCount()) * getXPCollectionType().getXpRatio();
        } else {
            fluidHandler = getFluidReceiver(router);
            if (fluidHandler == null) {
                fluidHandler = router.getFluidHandler();
            }
            spaceForXp = findSpaceForXPFluid(fluidHandler);
        }

        if (spaceForXp == 0) {
            return false;
        }

        List<ExperienceOrb> orbs = router.nonNullLevel().getEntitiesOfClass(
                ExperienceOrb.class,
                new AABB(getTarget().gPos.pos()).inflate(getRange()),
                Entity::isAlive
        );
        if (orbs.isEmpty()) {
            return false;
        }

        XPCollectionType xpCollectionType = getXPCollectionType();

        int initialSpaceForXp = spaceForXp;
        for (ExperienceOrb orb : orbs) {
            var rate = getItemsPerTick(router);
            while (orb.getValue() <= spaceForXp && rate > 0) {
                if (xpCollectionType.isSolid()) {
                    xpBuffered += orb.getValue();
                    if (xpBuffered > xpCollectionType.getXpRatio()) {
                        int count = xpBuffered / xpCollectionType.getXpRatio();
                        ItemStack stack = xpCollectionType.getIcon().copyWithCount(count);
                        ItemStack excess = router.insertBuffer(stack);
                        xpBuffered -= stack.getCount() * xpCollectionType.getXpRatio();
                        if (!excess.isEmpty()) {
                            InventoryUtils.dropItems(router.nonNullLevel(), Vec3.atCenterOf(router.getBlockPos()), excess);
                        }
                    }
                } else if (!doFluidXPFill(orb, fluidHandler)) {
                    spaceForXp = 0;
                }

                spaceForXp -= orb.getValue();
                orb.count--;
                rate--;

                if (orb.count <= 0) {
                    orb.remove(Entity.RemovalReason.DISCARDED);
                    break;
                }
            }
        }

        return initialSpaceForXp - spaceForXp > 0;
    }

    private IFluidHandler getFluidReceiver(ModularRouterBlockEntity router) {
        if (!xpMode || xpJuiceStack.isEmpty() || !(router.getLevel() instanceof ServerLevel serverLevel)) {
            return null;
        }

        if (fluidReceiverCache == null) {
            for (Direction face : MiscUtil.DIRECTIONS) {
                BlockPos pos = router.getBlockPos().relative(face);
                IFluidHandler handler = serverLevel.getCapability(Capabilities.FluidHandler.BLOCK, pos, face.getOpposite());
                if (handler != null && handler.fill(xpJuiceStack, IFluidHandler.FluidAction.SIMULATE) > 0) {
                    fluidReceiverCache = BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, serverLevel, pos, face.getOpposite(),
                            () -> true, () -> fluidReceiverCache = null);
                    break;
                }
            }
        }

        return fluidReceiverCache == null ? null : fluidReceiverCache.getCapability();
    }

    private boolean doFluidXPFill(ExperienceOrb orb, @Nullable IFluidHandler xpHandler) {
        if (xpHandler == null) {
            return false;
        }
        FluidStack xpStack = new FluidStack(xpJuiceStack.getFluid(), orb.getValue() * getXPCollectionType().getXpRatio() + xpBuffered);
        int filled = xpHandler.fill(xpStack, IFluidHandler.FluidAction.EXECUTE);
        if (filled < xpStack.getAmount()) {
            // tank is too full to store entire amount...
            xpBuffered = xpStack.getAmount() - filled;
            return false;
        } else {
            xpBuffered = 0;
            return true;
        }
    }

    private int findSpaceForXPFluid(@Nullable IFluidHandler xpHandler) {
        int space = 0;

        if (xpHandler != null) {
            for (int idx = 0; idx < xpHandler.getTanks(); idx++) {
                if (xpHandler.isFluidValid(idx, xpJuiceStack)) {
                    FluidStack fluidStack = xpHandler.getFluidInTank(idx);
                    if (fluidStack.isEmpty() || fluidStack.getFluid() == getXPCollectionType().getFluid()) {
                        space += (xpHandler.getTankCapacity(idx) - fluidStack.getAmount()) / getXPCollectionType().getXpRatio();
                    }
                }
            }
        }
        return space;
    }

    public XPCollectionType getXPCollectionType() {
        return settings.collectionType;
    }

    public boolean isAutoEjecting() {
        return settings.autoEject;
    }

    public boolean isXpMode() {
        return xpMode;
    }

    public record VacuumSettings(boolean autoEject, XPCollectionType collectionType) {
        public static final VacuumSettings DEFAULT = new VacuumSettings(false, XPCollectionType.BOTTLE_O_ENCHANTING);

        public static final Codec<VacuumSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.BOOL.optionalFieldOf("auto_eject", false).forGetter(VacuumSettings::autoEject),
                StringRepresentable.fromEnum(XPCollectionType::values).fieldOf("type").forGetter(VacuumSettings::collectionType)
        ).apply(builder, VacuumSettings::new));

        public static StreamCodec<FriendlyByteBuf, VacuumSettings> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, VacuumSettings::autoEject,
                NeoForgeStreamCodecs.enumCodec(XPCollectionType.class), VacuumSettings::collectionType,
                VacuumSettings::new
        );
    }
}
