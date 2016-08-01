package me.desht.modularrouters.logic.execution;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.AbstractModule;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class DropperExecutor extends ModuleExecutor {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStack stack = router.getBufferItemStack();
        if (stack != null && settings.getDirection() != AbstractModule.RelativeDirection.NONE && settings.getFilter().pass(stack)) {
            int nItems = router.getItemsPerTick();
            ItemStack toDrop = router.getBuffer().extractItem(0, nItems, true);
            BlockPos pos = router.getRelativeBlockPos(settings.getDirection());
            EntityItem item = new EntityItem(router.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, toDrop);
            item.motionX = item.motionY = item.motionZ = 0.0;
            if (router.getWorld().spawnEntityInWorld(item)) {
                router.getBuffer().extractItem(0, toDrop.stackSize, false);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
