package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.CompiledModule;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public class PullerModule extends Module {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModule compiled) {
        ItemStack bufferStack = router.getBufferItemStack();
        if (compiled.getDirection() != Module.RelativeDirection.NONE
                && (bufferStack == null || bufferStack.stackSize < bufferStack.getMaxStackSize())) {
            EnumFacing facingOpposite = compiled.getTarget().face;
            BlockPos pos = compiled.getTarget().pos;
            IItemHandler handler = InventoryUtils.getInventory(router.getWorld(), pos, facingOpposite);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack toExtract = handler.extractItem(i, router.getItemsPerTick(), true);
                    if (toExtract != null && compiled.getFilter().pass(toExtract)) {
                        ItemStack excess = router.getBuffer().insertItem(0, toExtract, false);
                        int taken = toExtract.stackSize - (excess == null ? 0 : excess.stackSize);
                        handler.extractItem(i, taken, false);
                        return taken > 0;
                    }
                }
            }
        }
        return false;
    }
}
