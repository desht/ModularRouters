package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class CompiledPullerModule2 extends CompiledPullerModule1 {
    public CompiledPullerModule2(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    protected List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        return Collections.singletonList(TargetedModule.getTarget(stack, !router.getLevel().isClientSide));
    }

    @Override
    boolean validateRange(TileEntityItemRouter router, ModuleTarget target) {
        return target != null
                && target.isSameWorld(router.getLevel())
                && router.getBlockPos().distSqr(target.gPos.pos()) <= getRangeSquared();
    }

    @Override
    protected void playParticles(TileEntityItemRouter router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
            router.addItemBeam(new BeamData(router.getTickRate(), targetPos, stack, 0x6080FF).reverseItems());
        }
    }
}
