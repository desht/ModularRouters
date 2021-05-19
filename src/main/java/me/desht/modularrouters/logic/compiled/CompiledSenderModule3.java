package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class CompiledSenderModule3 extends CompiledSenderModule2 {
    public CompiledSenderModule3(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean isRangeLimited() {
        return false;
    }

    @Override
    protected void playParticles(TileEntityItemRouter router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
            Direction facing = router.getAbsoluteFacing(ItemModule.RelativeDirection.FRONT);
            router.addItemBeam(new BeamData(router.getTickRate(), router.getBlockPos().relative(facing, 1), stack, 0x800080).fadeItems());
        }
    }
}
