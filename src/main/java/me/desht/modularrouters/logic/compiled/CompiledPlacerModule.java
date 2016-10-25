package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CompiledPlacerModule extends CompiledModule {
    public CompiledPlacerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (getDirection() == Module.RelativeDirection.NONE) {
            return false;
        }
        ItemStack toPlace = router.peekBuffer(1);
        if (toPlace == null || !getFilter().pass(toPlace)) {
            return false;
        }
        World w = router.getWorld();
        BlockPos pos = getTarget().pos;
        if (BlockUtil.tryPlaceAsBlock(toPlace, w, pos)) {
            if (Config.placerParticles) {
                w.playEvent(2001, pos, Block.getStateId(w.getBlockState(pos)));
            }
            router.extractBuffer(1);
            return true;
        } else {
            return false;
        }
    }
}
