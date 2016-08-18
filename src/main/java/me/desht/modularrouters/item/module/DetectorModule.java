package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import net.minecraft.item.ItemStack;

public class DetectorModule extends Module {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStack stack = router.getBufferItemStack();

        if (stack == null || !settings.getFilter().pass(stack)) {
            return false;
        }

        router.emitRedstone(settings.getDirection(), 15);

        return true;
    }
}
