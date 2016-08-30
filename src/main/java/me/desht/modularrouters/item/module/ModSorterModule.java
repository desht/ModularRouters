package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.CompiledModule;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ModSorterModule extends SorterModule {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModule compiled) {
        ItemStackHandler buffer = (ItemStackHandler) router.getBuffer();
        ItemStack bufferStack = buffer.getStackInSlot(0);

        if (bufferStack != null && compiled.getFilter().pass(bufferStack)) {
            IItemHandler handler = findTargetInventory(router, compiled);
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
        String reg = stack.getItem().getRegistryName().toString();
        return reg.substring(0, reg.indexOf(':'));
    }
}
