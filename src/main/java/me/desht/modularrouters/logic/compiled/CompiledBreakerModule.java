package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.IPickaxeUser;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.ModuleHelper;
import me.desht.modularrouters.util.TranslatableEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class CompiledBreakerModule extends CompiledModule {
    public static final String NBT_MATCH_TYPE = "MatchType";

    private final ItemStack pickaxe;
    private final MatchType matchType;

    public CompiledBreakerModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        pickaxe = ((IPickaxeUser) stack.getItem()).getPickaxe(stack);

        CompoundTag compound = ModuleHelper.validateNBT(stack);
        matchType = MatchType.values()[compound.getInt(NBT_MATCH_TYPE)];

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
            BlockUtil.BreakResult breakResult = BlockUtil.tryBreakBlock(router, world, pos, getFilter(), pickaxe, matchType == MatchType.BLOCK);
            if (breakResult.isBlockBroken()) {
                breakResult.processDrops(world, pos, router.getBuffer());
                if (ConfigHolder.common.module.breakerParticles.get() && router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) == 0) {
                    world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(oldState));
                }
                return true;
            }
        }
        return false;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public enum MatchType implements TranslatableEnum {
        ITEM,
        BLOCK;

        @Override
        public String getTranslationKey() {
            return "modularrouters.guiText.label.breakMatchType." + this;
        }
    }
}
