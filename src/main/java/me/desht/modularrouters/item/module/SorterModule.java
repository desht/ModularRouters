package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class SorterModule extends Module {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStackHandler buffer = (ItemStackHandler) router.getBuffer();
        ItemStack bufferStack = buffer.getStackInSlot(0);

        if (bufferStack != null && settings.getFilter().pass(bufferStack)) {
            IItemHandler handler = findTargetInventory(router, settings);
            if (handler == null) {
                return false;
            }
            for (int i = 0; i < handler.getSlots(); i++) {
                if (ItemHandlerHelper.canItemStacksStack(handler.getStackInSlot(i), bufferStack)) {
                    int sent = InventoryUtils.transferItems(buffer, handler, 0, router.getItemsPerTick());
                    return sent > 0;
                }
            }
        }
        return false;
    }

    private IItemHandler findTargetInventory(TileEntityItemRouter router, CompiledModuleSettings settings) {
        if (settings.getDirection() == Module.RelativeDirection.NONE) {
            return null;
        }
        BlockPos pos = router.getRelativeBlockPos(settings.getDirection());
        EnumFacing facing = router.getAbsoluteFacing(settings.getDirection());
        EnumFacing facingOpposite = facing.getOpposite();
        return InventoryUtils.getInventory(router.getWorld(), pos, facingOpposite);
    }
}
