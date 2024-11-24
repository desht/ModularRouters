package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class CompiledSenderModule3 extends CompiledSenderModule2 {
    public CompiledSenderModule3(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    protected void playParticles(ModularRouterBlockEntity router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
            Direction facing = router.getAbsoluteFacing(RelativeDirection.FRONT);
            router.addItemBeam(new BeamData.Builder(router, router.getBlockPos().relative(facing, 1), 0x800080)
                    .fade(true)
                    .withItemStack(stack)
                    .build()
            );
        }
    }
}
