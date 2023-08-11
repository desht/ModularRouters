package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class CompiledSenderModule3 extends CompiledSenderModule2 {
    public CompiledSenderModule3(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    protected boolean validate(ModularRouterBlockEntity router, ModuleTarget target) {
        return target.isSameWorld(router.getLevel()) ||
                !ModularRouters.getDimensionBlacklist().test(target.gPos.dimension().location())
                        && !ModularRouters.getDimensionBlacklist().test(router.nonNullLevel().dimension().location());
    }

    @Override
    protected void playParticles(ModularRouterBlockEntity router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
            Direction facing = router.getAbsoluteFacing(ModuleItem.RelativeDirection.FRONT);
            router.addItemBeam(new BeamData(router.getTickRate(), router.getBlockPos().relative(facing, 1), stack, 0x800080).fadeItems());
        }
    }
}
