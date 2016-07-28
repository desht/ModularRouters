package me.desht.modularrouters.logic.execution;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.AbstractModule;
import me.desht.modularrouters.item.module.ItemVacuumModule;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class VacuumExecutor extends ModuleExecutor {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStackHandler buffer = (ItemStackHandler) router.getBuffer();
        ItemStack bufferStack = buffer.getStackInSlot(0);

        int range = ItemVacuumModule.getVacuumRange(router);
        int offset = settings.getDirection() == AbstractModule.RelativeDirection.NONE ? 0 : range;
        BlockPos centrePos = router.getPos().offset(router.getAbsoluteFacing(settings.getDirection()), offset);
        List<EntityItem> items = router.getWorld().getEntitiesWithinAABB(EntityItem.class,
                new AxisAlignedBB(centrePos.add(-range, -range, -range), centrePos.add(range + 1, range + 1, range + 1)));

        int toPickUp = router.getItemsPerTick();
        for (EntityItem item : items) {
            if (item.isDead || item.cannotPickup()) {
                continue;
            }
            ItemStack stack = item.getEntityItem();
            if ((bufferStack == null || ItemHandlerHelper.canItemStacksStack(stack, bufferStack)) && settings.getFilter().pass(stack)) {
                ItemStack vacuumed = stack.splitStack(router.getItemsPerTick());
                ItemStack excess = router.getBuffer().insertItem(0, vacuumed, false);
                int remaining = excess == null ? 0 : excess.stackSize;
                stack.stackSize += remaining;
                int inserted = vacuumed.stackSize - remaining;
                toPickUp -= inserted;
                if (stack.stackSize <= 0) {
                    item.setDead();
                }
                if (inserted > 0 && Config.vacuumParticles) {
                    ((WorldServer) router.getWorld()).spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, false, item.posX, item.posY + 0.25, item.posZ, 2, 0.0, 0.0, 0.0, 0.0);
                }
                if (toPickUp <= 0) {
                    break;
                }
            }
        }
        return toPickUp < router.getItemsPerTick();
    }
}
