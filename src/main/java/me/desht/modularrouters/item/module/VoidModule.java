package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import net.minecraft.item.ItemStack;

public class VoidModule extends Module {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStack stack = router.getBufferItemStack();
        if (stack != null && settings.getFilter().pass(stack)) {
            // bye bye items
            int toVoid = router.getItemsPerTick();
            ItemStack gone = router.getBuffer().extractItem(0, toVoid, false);
            return gone != null && gone.stackSize > 0;
        }
        return false;
    }

    @Override
    public boolean isDirectional() {
        return false;
    }
}
