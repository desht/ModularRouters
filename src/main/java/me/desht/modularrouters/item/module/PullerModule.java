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
            IItemHandler handler = InventoryUtils.getInventory(router.getWorld(), compiled.getTarget().pos, compiled.getTarget().face);
            if (handler != null) {
                int taken = InventoryUtils.extractItems(handler, compiled, router);
                return taken > 0;
            }
        }
        return false;
    }
}
