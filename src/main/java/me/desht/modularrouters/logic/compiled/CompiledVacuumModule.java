package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.integration.XPCollection.XPCollectionType;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
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

    private TileEntity fluidReceiver = null;
    private Direction fluidReceiverFace = null;

    // temporary small xp buffer (generally around an orb or less)
    // does not survive router recompilation...
    private int xpBuffered = 0;

    // form in which to collect XP orbs
    private final XPCollectionType xpCollectionType;

    public CompiledVacuumModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        fastPickup = getAugmentCount(ModItems.FAST_PICKUP_AUGMENT.get()) > 0;
        xpMode = getAugmentCount(ModItems.XP_VACUUM_AUGMENT.get()) > 0;

        CompoundNBT compound = stack.getTagElement(ModularRouters.MODID);
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
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        if (xpMode) {
            return handleXpMode(router);
        } else {
            return handleItemMode(router);
        }
    }

    @Override
    public void onNeighbourChange(TileEntityItemRouter router) {
        findFluidReceiver(router);
    }

    private void findFluidReceiver(TileEntityItemRouter router) {
        if (!xpMode || xpJuiceStack.isEmpty()) return;

        fluidReceiver = null;
        for (Direction face : Direction.values()) {
            TileEntity te = router.getLevel().getBlockEntity(router.getBlockPos().relative(face));
            if (te != null) {
                te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite()).ifPresent(handler -> {
                    if (handler.fill(xpJuiceStack, IFluidHandler.FluidAction.SIMULATE) > 0) {
                        fluidReceiver = te;
                        fluidReceiverFace = face.getOpposite();
                    }
                });
            }
            if (fluidReceiver != null) break;
        }
    }

    private boolean handleItemMode(TileEntityItemRouter router) {
        if (router.isBufferFull()) {
            return false;
        }

        ItemStack bufferStack = router.getBuffer().getStackInSlot(0);

        BlockPos centrePos = getTarget().gPos.pos();
        int range = getRange();
        List<ItemEntity> items = router.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AxisAlignedBB(centrePos.offset(-range, -range, -range), centrePos.offset(range + 1, range + 1, range + 1)));

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
                    item.remove();
                }
                if (inserted > 0 && MRConfig.Common.Module.vacuumParticles && router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
                    ((ServerWorld) router.getLevel()).sendParticles(ParticleTypes.CLOUD, item.getX(), item.getY() + 0.25, item.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                }
                if (toPickUp <= 0) {
                    break;
                }
            }
        }
        return toPickUp < getItemsPerTick(router);
    }

    private boolean handleXpMode(TileEntityItemRouter router) {
        BlockPos centrePos = getTarget().gPos.pos();
        int range = getRange();
        List<ExperienceOrbEntity> orbs = router.getLevel().getEntitiesOfClass(ExperienceOrbEntity.class,
                new AxisAlignedBB(centrePos).inflate(range));
        if (orbs.isEmpty()) {
            return false;
        }

        ItemStack inRouterStack = router.getBufferItemStack();

        int spaceForXp;
        LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();
        if (xpCollectionType.isSolid()) {
            if (!inRouterStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(inRouterStack, xpCollectionType.getIcon())) {
                return false;
            }
            spaceForXp = (inRouterStack.getMaxStackSize() - inRouterStack.getCount()) * xpCollectionType.getXpRatio();
        } else {
            if (fluidReceiver != null && fluidReceiver.isRemoved()) {
                findFluidReceiver(router);
            }
            lazyFluidHandler = fluidReceiver != null ?
                fluidReceiver.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, fluidReceiverFace) :
                router.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
            spaceForXp = lazyFluidHandler.map(this::findSpaceForXPFluid).orElse(0);
        }
        if (spaceForXp == 0) {
            return false;
        }

        int initialSpaceForXp = spaceForXp;
        for (ExperienceOrbEntity orb : orbs) {
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
                        InventoryUtils.dropItems(router.getLevel(), Vector3d.atCenterOf(router.getBlockPos()), excess);
                    }
                }
            } else {
                boolean filledAll = lazyFluidHandler.map(xpHandler -> doFluidXPFill(orb, xpHandler)).orElse(false);
                if (!filledAll) spaceForXp = 0;
            }
            spaceForXp -= orb.getValue();
            orb.remove();
        }

        return initialSpaceForXp - spaceForXp > 0;
    }

    private boolean doFluidXPFill(ExperienceOrbEntity orb, IFluidHandler xpHandler) {
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
    public List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        if (router == null) {
            return null;
        }
        ItemModule.RelativeDirection dir = getDirection();
        int offset = dir == ItemModule.RelativeDirection.NONE ? 0 : getRange() + 1;
        Direction facing = router.getAbsoluteFacing(dir);
        GlobalPos gPos = MiscUtil.makeGlobalPos(router.getLevel(), router.getBlockPos().relative(facing, offset));
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
