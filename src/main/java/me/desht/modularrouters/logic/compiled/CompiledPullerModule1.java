package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class CompiledPullerModule1 extends CompiledModule {
    public CompiledPullerModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        if (!router.isBufferFull()) {
            if (!validateRange(router, getTarget())) {
                return false;
            }
            IItemHandler handler = InventoryUtils.getInventory(router.getWorld(), getTarget().gPos.getPos(), getTarget().face);
            if (handler != null) {
                ItemStack taken = transferToRouter(handler, router);
                if (!taken.isEmpty()) {
                    if (ConfigHandler.MODULE.pullerParticles.get()) {
                        playParticles(router, getTarget().gPos.getPos(), taken);
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

    void playParticles(TileEntityItemRouter router, BlockPos targetPos, ItemStack stack) {
        // do nothing by default
    }
}
