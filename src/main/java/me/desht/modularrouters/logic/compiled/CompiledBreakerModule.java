package me.desht.modularrouters.logic.compiled;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.IPickaxeUser;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.TranslatableEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;

public class CompiledBreakerModule extends CompiledModule {
    private final ItemStack pickaxe;
    private final BreakerSettings settings;

    public CompiledBreakerModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        settings = stack.getOrDefault(ModDataComponents.BREAKER_SETTINGS, BreakerSettings.DEFAULT);
        pickaxe = IPickaxeUser.getPickaxe(stack);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        if (router.getLevel() instanceof ServerLevel level && isRegulationOK(router, true)) {
            BlockPos pos = getTarget().gPos.pos();
            BlockState oldState = level.getBlockState(pos);
            if (BlockUtil.tryBreakBlock(router, level, pos, getFilter(), pickaxe, getMatchType() == MatchType.BLOCK)) {
                if (ConfigHolder.common.module.breakerParticles.get() && router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) == 0) {
                    level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(oldState));
                }
                return true;
            }
        }
        return false;
    }

    public MatchType getMatchType() {
        return settings.matchType;
    }

    public enum MatchType implements TranslatableEnum, StringRepresentable {
        ITEM("item"),
        BLOCK("block");

        private final String name;

        MatchType(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.guiText.label.breakMatchType." + name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record BreakerSettings(MatchType matchType) {
        public static final BreakerSettings DEFAULT = new BreakerSettings(MatchType.ITEM);

        public static final Codec<BreakerSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                StringRepresentable.fromEnum(MatchType::values)
                        .optionalFieldOf("action", MatchType.ITEM)
                        .forGetter(BreakerSettings::matchType)

        ).apply(builder, BreakerSettings::new));

        public static final StreamCodec<FriendlyByteBuf, BreakerSettings> STREAM_CODEC = StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(MatchType.class), BreakerSettings::matchType,
                BreakerSettings::new
        );
    }
}
