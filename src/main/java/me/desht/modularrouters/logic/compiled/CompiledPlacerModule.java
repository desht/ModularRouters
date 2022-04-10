package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class CompiledPlacerModule extends CompiledModule {
    public CompiledPlacerModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        if (!isRegulationOK(router, false)) {
            return false;
        }
        ItemStack toPlace = router.peekBuffer(1);
        if (!getFilter().test(toPlace)) {
            return false;
        }
        Level level = router.nonNullLevel();
        BlockPos pos = getTarget().gPos.pos();
        BlockState newState = BlockUtil.tryPlaceAsBlock(router, toPlace, level, pos, getFacing());
        if (newState != null) {
            if (ConfigHolder.common.module.placerParticles.get() && router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) == 0) {
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(newState));
            }
            router.extractBuffer(1);
            return true;
        } else {
            return false;
        }
    }
}
