package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class CompiledPullerModule extends CompiledModule {
    public CompiledPullerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (getDirection() != Module.RelativeDirection.NONE && !router.isBufferFull()) {
            IItemHandler handler = InventoryUtils.getInventory(router.getWorld(), getTarget().pos, getTarget().face);
            if (handler != null) {
                int taken = transferItems(handler, router);
                return taken > 0;
            }
        }
        return false;
    }
}
