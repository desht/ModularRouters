package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.integration.XPCollection.XPCollectionType;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.ItemHandlerHelper;

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
    private XPCollectionType xpCollectionType;

    public CompiledVacuumModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        fastPickup = getAugmentCount(ItemAugment.AugmentType.FAST_PICKUP) > 0;
        xpMode = getAugmentCount(ItemAugment.AugmentType.XP_VACUUM) > 0;

        NBTTagCompound compound = stack.getTagCompound();
        xpCollectionType = XPCollectionType.values()[compound.getInteger(NBT_XP_FLUID_TYPE)];
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
    public boolean execute(TileEntityItemRouter router) {
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
            if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite())) {
                IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
                if (handler.fill(xpJuiceStack, false) > 0) {
                    fluidReceiver = te;
                    fluidReceiverFace = face.getOpposite();
                    return;
                }
            }
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
            if (item.isDead || (!fastPickup && item.cannotPickup())) {
                continue;
            }
            ItemStack stackOnGround = item.getItem();
            if ((bufferStack.isEmpty() || ItemHandlerHelper.canItemStacksStack(stackOnGround, bufferStack)) && getFilter().test(stackOnGround)) {
                int inRouter = bufferStack.getCount();
                int spaceInRouter = getRegulationAmount() > 0 ?
                        Math.min(stackOnGround.getMaxStackSize(), getRegulationAmount()) - inRouter :
                        stackOnGround.getMaxStackSize() - inRouter;
                ItemStack vacuumed = stackOnGround.splitStack(Math.min(getItemsPerTick(router), spaceInRouter));
                ItemStack excess = router.insertBuffer(vacuumed);
                int remaining = excess == null ? 0 : excess.getCount();
                stackOnGround.grow(remaining);
                int inserted = vacuumed.getCount() - remaining;
                toPickUp -= inserted;
                if (stackOnGround.isEmpty()) {
                    item.setDead();
                }
                if (inserted > 0 && ConfigHandler.module.vacuumParticles && router.getUpgradeCount(ItemUpgrade.UpgradeType.MUFFLER) < 2) {
                    ((WorldServer) router.getWorld()).spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, false, item.posX, item.posY + 0.25, item.posZ, 2, 0.0, 0.0, 0.0, 0.0);
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

        int spaceForXp = 0;
        IFluidHandler xpHandler = null;
        if (xpCollectionType.isSolid()) {
            if (!inRouterStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(inRouterStack, xpCollectionType.getIcon())) {
                return false;
            }
            spaceForXp = (inRouterStack.getMaxStackSize() - inRouterStack.getCount()) * xpCollectionType.getXpRatio();
        } else {
            if (fluidReceiver != null && fluidReceiver.isInvalid()) {
                findFluidReceiver(router);
            }

            xpHandler = fluidReceiver == null || !fluidReceiver.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, fluidReceiverFace) ?
                    FluidUtil.getFluidHandler(inRouterStack) :
                    fluidReceiver.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, fluidReceiverFace);
            if (xpHandler == null) {
                return false;
            }

            for (IFluidTankProperties tank : xpHandler.getTankProperties()) {
                if (tank.canFillFluidType(xpJuiceStack)) {
                    if (tank.getContents() == null || tank.getContents().amount == 0
                            || tank.getContents().getFluid().getName().equals(xpCollectionType.getRegistryName()))
                    spaceForXp += (tank.getCapacity() - (tank.getContents() == null ? 0 : tank.getContents().amount)) / xpCollectionType.getXpRatio();
                }
            }
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
                    ItemStack stack = xpCollectionType.getIcon().copy();
                    stack.setCount(xpBuffered / xpCollectionType.getXpRatio());
                    ItemStack excess = router.insertBuffer(stack);
                    xpBuffered -= stack.getCount() * xpCollectionType.getXpRatio();
                    if (!excess.isEmpty()) {
                        InventoryUtils.dropItems(router.getWorld(), router.getPos(), excess);
                    }
                }
            } else {
                FluidStack xpStack = new FluidStack(xpJuiceStack.getFluid(), orb.getXpValue() * xpCollectionType.getXpRatio() + xpBuffered);
                int filled = xpHandler.fill(xpStack, true);
                if (filled < xpStack.amount) {
                    // tank is too full to store entire amount...
                    spaceForXp = 0;
                    xpBuffered = xpStack.amount - filled;
                } else {
                    xpBuffered = 0;
                }
            }
            spaceForXp -= orb.getXpValue();
            orb.setDead();
        }

        return initialSpaceForXp - spaceForXp > 0;
    }

    @Override
    public ModuleTarget setupTarget(TileEntityItemRouter router, ItemStack stack) {
        if (router == null) {
            return null;
        }
        Module.RelativeDirection dir = getDirection();
        int offset = dir == Module.RelativeDirection.NONE ? 0 : getRange() + 1;
        EnumFacing facing = router.getAbsoluteFacing(dir);
        return new ModuleTarget(router.getWorld().provider.getDimension(), router.getPos().offset(facing, offset), facing);
    }

    public XPCollectionType getXPCollectionType() {
        return xpCollectionType;
    }

    public boolean isAutoEjecting() {
        return autoEjecting;
    }
}
