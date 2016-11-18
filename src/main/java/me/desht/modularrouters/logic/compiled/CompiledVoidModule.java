package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;

public class CompiledVoidModule extends CompiledModule  {
    public CompiledVoidModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        ItemStack stack = router.getBufferItemStack();
        if (stack != null && getFilter().pass(stack)) {
            // bye bye items
            int toVoid = Math.min(router.getItemsPerTick(), stack.stackSize - getRegulationAmount());
            if (toVoid <= 0) {
                return false;
            }
            ItemStack gone = router.getBuffer().extractItem(0, toVoid, false);
            return gone != null && gone.stackSize > 0;
        }
        return false;
    }
}
