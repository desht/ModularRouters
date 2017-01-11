package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public class CompiledPullerModule extends CompiledModule {
    public CompiledPullerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (!router.isBufferFull()) {
            IItemHandler handler = InventoryUtils.getInventory(router.getWorld(), getTarget().pos, getTarget().face);
            if (handler != null) {
                int taken = transferToRouter(handler, router);
                if (taken > 0) {
                    if (Config.pullerParticles && router.getUpgradeCount(ItemUpgrade.UpgradeType.MUFFLER) < 2) {
                        playParticles(router, getTarget().pos);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    protected void playParticles(TileEntityItemRouter router, BlockPos targetPos) {
        // do nothing by default
    }
}
