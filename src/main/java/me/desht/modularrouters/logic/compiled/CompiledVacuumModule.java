package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.integration.XPCollection.XPCollectionType;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class CompiledVacuumModule extends CompiledModule {
    public static final String NBT_XP_FLUID_TYPE = "XPFluidType";
    public static final String NBT_AUTO_EJECT = "AutoEject";

    private final boolean fastPickup;
    private final boolean xpMode;
    private final boolean autoEjecting;
    private final FluidStack xpJuiceStack;

    private BlockEntity fluidReceiver = null;
    private Direction fluidReceiverFace = null;

    // temporary small xp buffer (generally around an orb or less)
    // does not survive router recompilation...
    private int xpBuffered = 0;

    // form in which to collect XP orbs
    private final XPCollectionType xpCollectionType;

    public CompiledVacuumModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
        fastPickup = getAugmentCount(ModItems.FAST_PICKUP_AUGMENT.get()) > 0;
        xpMode = getAugmentCount(ModItems.XP_VACUUM_AUGMENT.get()) > 0;

        CompoundTag compound = stack.getTagElement(ModularRouters.MODID);
        if (compound != null) {
            xpCollectionType = XPCollection.getXPType(compound.getInt(NBT_XP_FLUID_TYPE));
            autoEjecting = compound.getBoolean(NBT_AUTO_EJECT);

            if (xpMode) {
                Fluid xpFluid = xpCollectionType.getFluid();
                xpJuiceStack = xpFluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(xpFluid, 1000);
                if (router != null) {
                    findFluidReceiver(router);
                }
            } else {
                xpJuiceStack = FluidStack.EMPTY;
            }
        } else {
            xpCollectionType = XPCollectionType.BOTTLE_O_ENCHANTING;
            autoEjecting = false;
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
        findFluidReceiver(router);
    }

    private void findFluidReceiver(ModularRouterBlockEntity router) {
        if (!xpMode || xpJuiceStack.isEmpty()) return;

        fluidReceiver = null;
        for (Direction face : MiscUtil.DIRECTIONS) {
            BlockEntity te = router.nonNullLevel().getBlockEntity(router.getBlockPos().relative(face));
            if (te != null) {
                te.getCapability(ForgeCapabilities.FLUID_HANDLER, face.getOpposite()).ifPresent(handler -> {
                    if (handler.fill(xpJuiceStack, IFluidHandler.FluidAction.SIMULATE) > 0) {
                        fluidReceiver = te;
                        fluidReceiverFace = face.getOpposite();
                    }
                });
            }
            if (fluidReceiver != null) break;
        }
    }

    private boolean handleItemMode(ModularRouterBlockEntity router) {
        if (router.isBufferFull()) {
            return false;
        }

        ItemStack bufferStack = router.getBuffer().getStackInSlot(0);

        BlockPos centrePos = getTarget().gPos.pos();
        int range = getRange();
        List<ItemEntity> items = router.nonNullLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(centrePos.offset(-range, -range, -range), centrePos.offset(range + 1, range + 1, range + 1)));

        int toPickUp = getItemsPerTick(router);

        for (ItemEntity item : items) {
            if (!item.isAlive() || (!fastPickup && item.hasPickUpDelay())) {
                continue;
            }
            ItemStack stackOnGround = item.getItem();
            if ((bufferStack.isEmpty() || ItemHandlerHelper.canItemStacksStack(stackOnGround, bufferStack)) && getFilter().test(stackOnGround)) {
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
        LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

        if (xpCollectionType.isSolid()) {
            ItemStack inRouterStack = router.getBufferItemStack();
            if (!inRouterStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(inRouterStack, xpCollectionType.getIcon())) {
                return false;
            }
            spaceForXp = (inRouterStack.getMaxStackSize() - inRouterStack.getCount()) * xpCollectionType.getXpRatio();
        } else {
            if (fluidReceiver != null && fluidReceiver.isRemoved()) {
                findFluidReceiver(router);
            }
            lazyFluidHandler = fluidReceiver != null ?
                fluidReceiver.getCapability(ForgeCapabilities.FLUID_HANDLER, fluidReceiverFace) :
                router.getCapability(ForgeCapabilities.FLUID_HANDLER);
            spaceForXp = lazyFluidHandler.map(this::findSpaceForXPFluid).orElse(0);
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

        int initialSpaceForXp = spaceForXp;
        for (ExperienceOrb orb : orbs) {
            if (orb.getValue() > spaceForXp) {
                break;
            }
            if (xpCollectionType.isSolid()) {
                xpBuffered += orb.getValue();
                if (xpBuffered > xpCollectionType.getXpRatio()) {
                    int count = xpBuffered / xpCollectionType.getXpRatio();
                    ItemStack stack = ItemHandlerHelper.copyStackWithSize(xpCollectionType.getIcon(), count);
                    ItemStack excess = router.insertBuffer(stack);
                    xpBuffered -= stack.getCount() * xpCollectionType.getXpRatio();
                    if (!excess.isEmpty()) {
                        InventoryUtils.dropItems(router.nonNullLevel(), Vec3.atCenterOf(router.getBlockPos()), excess);
                    }
                }
            } else {
                boolean filledAll = lazyFluidHandler.map(xpHandler -> doFluidXPFill(orb, xpHandler)).orElse(false);
                if (!filledAll) spaceForXp = 0;
            }
            spaceForXp -= orb.getValue();
            orb.remove(Entity.RemovalReason.DISCARDED);
        }

        return initialSpaceForXp - spaceForXp > 0;
    }

    private boolean doFluidXPFill(ExperienceOrb orb, IFluidHandler xpHandler) {
        FluidStack xpStack = new FluidStack(xpJuiceStack.getFluid(), orb.getValue() * xpCollectionType.getXpRatio() + xpBuffered);
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

    private int findSpaceForXPFluid(IFluidHandler xpHandler) {
        int space = 0;

        for (int idx = 0; idx < xpHandler.getTanks(); idx++) {
            if (xpHandler.isFluidValid(idx, xpJuiceStack)) {
                FluidStack fluidStack = xpHandler.getFluidInTank(idx);
                if (fluidStack.isEmpty() || fluidStack.getFluid() == xpCollectionType.getFluid()) {
                    space += (xpHandler.getTankCapacity(idx) - fluidStack.getAmount()) / xpCollectionType.getXpRatio();
                }
            }
        }
        return space;
    }

    @Override
    public List<ModuleTarget> setupTargets(ModularRouterBlockEntity router, ItemStack stack) {
        if (router == null) {
            return null;
        }
        ModuleItem.RelativeDirection dir = getDirection();
        int offset = dir == ModuleItem.RelativeDirection.NONE ? 0 : getRange() + 1;
        Direction facing = router.getAbsoluteFacing(dir);
        GlobalPos gPos = MiscUtil.makeGlobalPos(router.nonNullLevel(), router.getBlockPos().relative(facing, offset));
        return Collections.singletonList(new ModuleTarget(gPos, facing));
    }

    public XPCollectionType getXPCollectionType() {
        return xpCollectionType;
    }

    public boolean isAutoEjecting() {
        return autoEjecting;
    }

    public boolean isXpMode() {
        return xpMode;
    }
}
