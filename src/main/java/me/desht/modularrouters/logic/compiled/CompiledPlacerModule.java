package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CompiledPlacerModule extends CompiledModule {
    public CompiledPlacerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (!isRegulationOK(router, false)) {
            return false;
        }
        ItemStack toPlace = router.peekBuffer(1);
        if (!getFilter().test(toPlace)) {
            return false;
        }
        World w = router.getWorld();
        BlockPos pos = getTarget().pos;
        IBlockState newState = BlockUtil.tryPlaceAsBlock(toPlace, w, pos, getFacing(), getRouterFacing());
        if (newState != null) {
            if (ConfigHandler.module.placerParticles && router.getUpgradeCount(ItemUpgrade.UpgradeType.MUFFLER) == 0) {
                w.playEvent(2001, pos, Block.getStateId(newState));
            }
            router.extractBuffer(1);
            return true;
        } else {
            return false;
        }
    }
}
