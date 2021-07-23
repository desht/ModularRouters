package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.IPickaxeUser;
import me.desht.modularrouters.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class CompiledBreakerModule extends CompiledModule {
    private final ItemStack pickaxe;

    public CompiledBreakerModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        pickaxe = ((IPickaxeUser) stack.getItem()).getPickaxe(stack);

        // backwards compat
        if (!EnchantmentHelper.getEnchantments(stack).isEmpty() && EnchantmentHelper.getEnchantments(pickaxe).isEmpty()) {
            EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack), pickaxe);
        }
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        if (isRegulationOK(router, true)) {
            Level world = router.getLevel();
            if (!(world instanceof ServerLevel)) {
                return false;
            }
            BlockPos pos = getTarget().gPos.pos();
            BlockState oldState = world.getBlockState(pos);
            BlockUtil.BreakResult breakResult = BlockUtil.tryBreakBlock(router, world, pos, getFilter(), pickaxe);
            if (breakResult.isBlockBroken()) {
                breakResult.processDrops(world, pos, router.getBuffer());
                if (MRConfig.Common.Module.breakerParticles && router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) == 0) {
                    world.levelEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getId(oldState));
                }
                return true;
            }
        }
        return false;
    }
}
