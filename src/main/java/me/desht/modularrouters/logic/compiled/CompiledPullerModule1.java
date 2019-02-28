package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public class CompiledPullerModule1 extends CompiledModule {
    public CompiledPullerModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (!router.isBufferFull()) {
            if (!validateRange(router, getTarget())) {
                return false;
            }
            IItemHandler handler = InventoryUtils.getInventory(router.getWorld(), getTarget().pos, getTarget().face);
            if (handler != null) {
                int taken = transferToRouter(handler, router);
                if (taken > 0) {
                    if (ConfigHandler.MODULE.pullerParticles.get()) {
                        playParticles(router, getTarget().pos);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    boolean validateRange(TileEntityItemRouter router, ModuleTarget target) {
        return true;
    }

    protected void playParticles(TileEntityItemRouter router, BlockPos targetPos) {
        // do nothing by default
    }
}
