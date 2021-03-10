package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

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
            ModuleTarget target = getTarget();
            if (target == null) return false;
            return getTarget().getItemHandler().map(handler -> {
                ItemStack taken = transferToRouter(handler, router);
                if (!taken.isEmpty()) {
                    if (MRConfig.Common.Module.pullerParticles) {
                        playParticles(router,  getTarget().gPos.pos(), taken);
                    }
                    return true;
                }
                return false;
            }).orElse(false);
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
