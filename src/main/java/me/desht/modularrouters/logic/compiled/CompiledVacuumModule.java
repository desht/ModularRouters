package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.integration.XPCollection.XPCollectionType;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Particles;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
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
    private EnumFacing fluidReceiverFace = null;

    // temporary small xp buffer (generally around an orb or less)
    // does not survive router recompilation...
    private int xpBuffered = 0;

    // form in which to collect XP orbs
    private final XPCollectionType xpCollectionType;

    public CompiledVacuumModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        fastPickup = getAugmentCount(ObjectRegistry.FAST_PICKUP_AUGMENT) > 0;
        xpMode = getAugmentCount(ObjectRegistry.XP_VACUUM_AUGMENT) > 0;

        NBTTagCompound compound = stack.getTag();
        xpCollectionType = XPCollectionType.values()[compound.getInt(NBT_XP_FLUID_TYPE)];
        autoEjecting = compound.getBoolean(NBT_AUTO_EJECT);

        if (xpMode) {
            Fluid xpFluid = xpCollectionType.getFluid();
            xpJuiceStack = xpFluid == null ? null : new FluidStack(xpFluid, 1000);
            if (router != null) {
                findFluidReceiver(router);
            }
        } else {
            xpJuiceStack = null;
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
        if (!xpMode || xpJuiceStack == null) return;

        fluidReceiver = null;
        for (EnumFacing face : EnumFacing.values()) {
            TileEntity te = router.getWorld().getTileEntity(router.getPos().offset(face));
            if (te != null) {
                te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite()).ifPresent(handler -> {
                    if (handler.fill(xpJuiceStack, false) > 0) {
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

        BlockPos centrePos = getTarget().pos;
        int range = getRange();
        List<EntityItem> items = router.getWorld().getEntitiesWithinAABB(EntityItem.class,
                new AxisAlignedBB(centrePos.add(-range, -range, -range), centrePos.add(range + 1, range + 1, range + 1)));

        int toPickUp = getItemsPerTick(router);

        for (EntityItem item : items) {
            if (item.removed || (!fastPickup && item.cannotPickup())) {
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
                if (inserted > 0 && ConfigHandler.MODULE.vacuumParticles.get() && router.getUpgradeCount(ObjectRegistry.MUFFLER_UPGRADE) < 2) {
                    ((WorldServer) router.getWorld()).spawnParticle(Particles.CLOUD, item.posX, item.posY + 0.25, item.posZ, 2, 0.0, 0.0, 0.0, 0.0);
                }
                if (toPickUp <= 0) {
                    break;
                }
            }
        }
        return toPickUp < getItemsPerTick(router);
    }

    private boolean handleXpMode(TileEntityItemRouter router) {
        BlockPos centrePos = getTarget().pos;
        int range = getRange();
        List<EntityXPOrb> orbs = router.getWorld().getEntitiesWithinAABB(EntityXPOrb.class,
                new AxisAlignedBB(centrePos.add(-range, -range, -range), centrePos.add(range + 1, range + 1, range + 1)));
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
        for (EntityXPOrb orb : orbs) {
            if (orb.getXpValue() > spaceForXp) {
                break;
            }
            if (xpCollectionType.isSolid()) {
                xpBuffered += orb.getXpValue();
                if (xpBuffered > xpCollectionType.getXpRatio()) {
                    int count = xpBuffered / xpCollectionType.getXpRatio();
                    ItemStack stack = ItemHandlerHelper.copyStackWithSize(xpCollectionType.getIcon(), count);
                    ItemStack excess = router.insertBuffer(stack);
                    xpBuffered -= stack.getCount() * xpCollectionType.getXpRatio();
                    if (!excess.isEmpty()) {
                        InventoryUtils.dropItems(router.getWorld(), router.getPos(), excess);
                    }
                }
            } else {
                boolean filledAll = lazyFluidHandler.map(xpHandler -> doFluidXPFill(orb, xpHandler)).orElse(false);
                if (!filledAll) spaceForXp = 0;
            }
            spaceForXp -= orb.getXpValue();
            orb.remove();
        }

        return initialSpaceForXp - spaceForXp > 0;
    }

    private boolean doFluidXPFill(EntityXPOrb orb, IFluidHandler xpHandler) {
        FluidStack xpStack = new FluidStack(xpJuiceStack.getFluid(), orb.getXpValue() * xpCollectionType.getXpRatio() + xpBuffered);
        int filled = xpHandler.fill(xpStack, true);
        if (filled < xpStack.amount) {
            // tank is too full to store entire amount...
            xpBuffered = xpStack.amount - filled;
            return false;
        } else {
            xpBuffered = 0;
            return true;
        }
    }

    private int findSpaceForXPFluid(IFluidHandler xpHandler) {
        int space = 0;
        for (IFluidTankProperties tank : xpHandler.getTankProperties()) {
            if (tank.canFillFluidType(xpJuiceStack)) {
                if (tank.getContents() == null || tank.getContents().amount == 0
                        || tank.getContents().getFluid().getName().equals(xpCollectionType.getRegistryName()))
                    space += (tank.getCapacity() - (tank.getContents() == null ? 0 : tank.getContents().amount)) / xpCollectionType.getXpRatio();
            }
        }
        return space;
    }

    @Override
    public List<ModuleTarget> setupTarget(TileEntityItemRouter router, ItemStack stack) {
        if (router == null) {
            return null;
        }
        ItemModule.RelativeDirection dir = getDirection();
        int offset = dir == ItemModule.RelativeDirection.NONE ? 0 : getRange() + 1;
        EnumFacing facing = router.getAbsoluteFacing(dir);
        return Collections.singletonList(new ModuleTarget(MiscUtil.getDimensionForWorld(router.getWorld()), router.getPos().offset(facing, offset), facing));
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
