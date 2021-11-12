package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.IHasTranslationKey;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.IPickaxeUser;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class CompiledBreakerModule extends CompiledModule {
    public static final String NBT_MATCH_TYPE = "MatchType";

    private final ItemStack pickaxe;
    private final MatchType matchType;

    public CompiledBreakerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        pickaxe = ((IPickaxeUser) stack.getItem()).getPickaxe(stack);

        CompoundNBT compound = ModuleHelper.validateNBT(stack);
        matchType = MatchType.values()[compound.getInt(NBT_MATCH_TYPE)];

        // backwards compat
        if (!EnchantmentHelper.getEnchantments(stack).isEmpty() && EnchantmentHelper.getEnchantments(pickaxe).isEmpty()) {
            EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack), pickaxe);
        }
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        if (isRegulationOK(router, true)) {
            World world = router.getLevel();
            if (!(world instanceof ServerWorld)) {
                return false;
            }
            BlockPos pos = getTarget().gPos.pos();
            BlockState oldState = world.getBlockState(pos);
            BlockUtil.BreakResult breakResult = BlockUtil.tryBreakBlock(router, world, pos, getFilter(), pickaxe, matchType == MatchType.BLOCK);
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

    public MatchType getMatchType() {
        return matchType;
    }

    public enum MatchType implements IHasTranslationKey {
        ITEM,
        BLOCK;

        @Override
        public String getTranslationKey() {
            return "modularrouters.guiText.label.breakMatchType." + this;
        }
    }
}
