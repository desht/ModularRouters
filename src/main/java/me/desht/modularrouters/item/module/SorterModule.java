package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.CompiledModule;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class SorterModule extends Module {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModule compiled) {
        ItemStackHandler buffer = (ItemStackHandler) router.getBuffer();
        ItemStack bufferStack = buffer.getStackInSlot(0);

        if (bufferStack != null && compiled.getFilter().pass(bufferStack)) {
            IItemHandler handler = findTargetInventory(router, compiled);
            if (handler == null) {
                return false;
            }
            for (int i = 0; i < handler.getSlots(); i++) {
                int pos = compiled.getLastMatchPos(i, handler.getSlots());
                if (ItemHandlerHelper.canItemStacksStack(handler.getStackInSlot(pos), bufferStack)) {
                    compiled.setLastMatchPos(pos);
                    int sent = InventoryUtils.transferItems(buffer, handler, 0, router.getItemsPerTick());
                    return sent > 0;
                }
            }
        }
        return false;
    }

    protected IItemHandler findTargetInventory(TileEntityItemRouter router, CompiledModule settings) {
        if (settings.getDirection() == Module.RelativeDirection.NONE) {
            return null;
        }
        return InventoryUtils.getInventory(router.getWorld(), settings.getTarget().pos, settings.getTarget().face);
    }
}
