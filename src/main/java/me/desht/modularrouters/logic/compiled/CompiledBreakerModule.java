package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.Lists;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public class CompiledBreakerModule extends CompiledModule {
    private final boolean silkTouch;
    private final int fortune;

    public CompiledBreakerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
        fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (getDirection() != Module.RelativeDirection.NONE && !router.isBufferFull() && isRegulationOK(router, true)) {
            World world = router.getWorld();
            if (!(world instanceof WorldServer)) {
                return false;
            }
            BlockPos pos = getTarget().pos;
            List<ItemStack> drops = Lists.newArrayList();
            IBlockState state = world.getBlockState(pos);
            if (BlockUtil.tryBreakBlock(world, pos, getFilter(), drops, silkTouch, fortune)) {
                for (ItemStack drop : drops) {
                    ItemStack excess = router.getBuffer().insertItem(0, drop, false);
                    if (excess != null) {
                        dropItems(world, pos, excess);
                    }
                }
                if (Config.breakerParticles && router.getUpgradeCount(ItemUpgrade.UpgradeType.MUFFLER) == 0) {
                    world.playEvent(2001, pos, Block.getStateId(state));
                }
                return true;
            }
        }
        return false;
    }

    private void dropItems(World world, BlockPos pos, ItemStack stack) {
        EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        world.spawnEntityInWorld(item);
    }
}
