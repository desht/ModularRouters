package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class CompiledPullerModule2 extends CompiledPullerModule1 {
    public CompiledPullerModule2(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    protected void playParticles(ModularRouterBlockEntity router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
            router.addItemBeam(new BeamData.Builder(router, targetPos, 0x6080FF).reversed(true).withItemStack(stack).build());
        }
    }
}
