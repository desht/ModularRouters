package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class CompiledModSorterModule extends CompiledSorterModule {
    public CompiledModSorterModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        IItemHandler buffer = router.getBuffer();
        ItemStack bufferStack = buffer.getStackInSlot(0);

        if (bufferStack != null && getFilter().pass(bufferStack)) {
            IItemHandler handler = findTargetInventory(router);
            if (handler == null) {
                return false;
            }
            String mod = getMod(bufferStack);
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack != null) {
                    String mod2 = getMod(stack);
                    if (mod.equals(mod2)) {
                        int sent = InventoryUtils.transferItems(buffer, handler, 0, router.getItemsPerTick());
                        return sent > 0;
                    }
                }
            }
        }
        return false;
    }

    private String getMod(ItemStack stack) {
        return stack.getItem().getRegistryName().getResourceDomain();
    }
}
