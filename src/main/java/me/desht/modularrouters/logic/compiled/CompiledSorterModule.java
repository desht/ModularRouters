package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class CompiledSorterModule extends CompiledModule {
    public CompiledSorterModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        ItemStackHandler buffer = (ItemStackHandler) router.getBuffer();
        ItemStack bufferStack = buffer.getStackInSlot(0);

        if (getFilter().pass(bufferStack)) {
            IItemHandler handler = findTargetInventory(router);
            if (handler == null) {
                return false;
            }
            for (int i = 0; i < handler.getSlots(); i++) {
                int pos = getLastMatchPos(i, handler.getSlots());
                if (bufferStack.isItemEqualIgnoreDurability(handler.getStackInSlot(pos))) {
                    setLastMatchPos(pos);
                    int sent = InventoryUtils.transferItems(buffer, handler, 0, router.getItemsPerTick());
                    return sent > 0;
                }
            }
        }
        return false;
    }

    IItemHandler findTargetInventory(TileEntityItemRouter router) {
        if (getDirection() == Module.RelativeDirection.NONE) {
            return null;
        }
        return InventoryUtils.getInventory(router.getWorld(), getTarget().pos, getTarget().face);
    }
}
