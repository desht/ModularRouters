package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ValidatingSlot;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.VacuumModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class CompiledVacuumModule extends CompiledModule {
    private final boolean fastPickup;

    public CompiledVacuumModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        fastPickup = ModuleHelper.hasFastPickup(stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (router.isBufferFull()) {
            return false;
        }

        ItemStack bufferStack = router.getBuffer().getStackInSlot(0);

        int range = VacuumModule.getVacuumRange(router);
        BlockPos centrePos = getTarget().pos;
        List<EntityItem> items = router.getWorld().getEntitiesWithinAABB(EntityItem.class,
                new AxisAlignedBB(centrePos.add(-range, -range, -range), centrePos.add(range + 1, range + 1, range + 1)));

        int toPickUp = router.getItemsPerTick();

        for (EntityItem item : items) {
            if (item.isDead || (!fastPickup && item.cannotPickup())) {
                continue;
            }
            ItemStack stackOnGround = item.getEntityItem();
            if ((bufferStack.isEmpty() || ItemHandlerHelper.canItemStacksStack(stackOnGround, bufferStack)) && getFilter().pass(stackOnGround)) {
                int inRouter = bufferStack.getCount();
                int spaceInRouter = getRegulationAmount() > 0 ?
                        Math.min(stackOnGround.getMaxStackSize(), getRegulationAmount()) - inRouter :
                        stackOnGround.getMaxStackSize() - inRouter;
                ItemStack vacuumed = stackOnGround.splitStack(Math.min(router.getItemsPerTick(), spaceInRouter));
                ItemStack excess = router.insertBuffer(vacuumed);
                int remaining = excess == null ? 0 : excess.getCount();
                stackOnGround.grow(remaining);
                int inserted = vacuumed.getCount() - remaining;
                toPickUp -= inserted;
                if (stackOnGround.isEmpty()) {
                    item.setDead();
                }
                if (inserted > 0 && Config.vacuumParticles && router.getUpgradeCount(ItemUpgrade.UpgradeType.MUFFLER) < 2) {
                    ((WorldServer) router.getWorld()).spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, false, item.posX, item.posY + 0.25, item.posZ, 2, 0.0, 0.0, 0.0, 0.0);
                }
                if (toPickUp <= 0) {
                    break;
                }
            }
        }
        return toPickUp < router.getItemsPerTick();
    }

    @Override
    public ModuleTarget setupTarget(TileEntityItemRouter router, ItemStack stack) {
        if (router == null) {
            return null;
        }
        Module.RelativeDirection dir = getDirection();
        int offset = dir == Module.RelativeDirection.NONE ? 0 : VacuumModule.getVacuumRange(router) + 1;
        EnumFacing facing = router.getAbsoluteFacing(dir);
        return new ModuleTarget(router.getWorld().provider.getDimension(), router.getPos().offset(facing, offset), facing);
    }
}
