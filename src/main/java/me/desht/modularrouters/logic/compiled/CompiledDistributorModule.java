package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.ITargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.settings.TransferDirection;
import me.desht.modularrouters.util.BeamData;
import me.desht.modularrouters.util.TranslatableEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class CompiledDistributorModule extends CompiledSenderModule2 {
    private final DistributorSettings settings;
    private int nextTarget;

    public CompiledDistributorModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        settings = stack.getOrDefault(ModDataComponents.DISTRIBUTOR_SETTINGS, DistributorSettings.DEFAULT);
        nextTarget = settings.strategy == DistributionStrategy.FURTHEST_FIRST ? getTargets().size() - 1 : 0;
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        return isPulling() ? executePull(router) : super.execute(router);
    }

    private boolean executePull(ModularRouterBlockEntity router) {
        if (router.isBufferFull()) return false;

        ModuleTarget tgt = getEffectiveTarget(router);
        if (tgt == null) return false;
        return tgt.getItemHandler().map(handler -> {
            ItemStack taken = transferToRouter(handler, tgt.gPos.pos(), router);
            if (!taken.isEmpty()) {
                if (ConfigHolder.common.module.pullerParticles.get()) {
                    playParticles(router, tgt.gPos.pos(), taken);
                }
                return true;
            }
            return false;
        }).orElse(false);
    }

    public boolean isPulling() {
        return settings.direction == TransferDirection.TO_ROUTER;
    }

    public DistributionStrategy getDistributionStrategy() {
        return settings.strategy;
    }

    @Override
    void playParticles(ModularRouterBlockEntity router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
            BeamData d = new BeamData.Builder(router, targetPos, getBeamColor())
                    .reversed(isPulling())
                    .withItemStack(stack)
                    .build();
            router.addItemBeam(d);
        }
    }

    @Override
    protected int getBeamColor() {
        return isPulling() ? 0x6080FF :  super.getBeamColor();
    }

    @Override
    protected List<ModuleTarget> setupTargets(ModularRouterBlockEntity router, ItemStack stack) {
        Set<ModuleTarget> t = ITargetedModule.getTargets(stack, router != null && !router.nonNullLevel().isClientSide);
        List<ModuleTarget> l = Lists.newArrayList(t);
        if (router == null) return l;
        l.sort(Comparator.comparingDouble(o -> calcDist(o, router)));
        return l;
    }

    private static double calcDist(ModuleTarget tgt, @Nonnull BlockEntity blockEntity) {
        double distance = tgt.gPos.pos().distSqr(blockEntity.getBlockPos());
        if (!tgt.isSameWorld(blockEntity.getLevel())) {
            distance += 100_000_000;  // cross-dimension penalty
        }
        return distance;
    }

    @Override
    public ModuleTarget getEffectiveTarget(ModularRouterBlockEntity router) {
        if (getTargets() == null || getTargets().isEmpty()) return null;
        int nTargets = getTargets().size();
        if (nTargets == 1) return getTargets().getFirst(); // degenerate case

        ModuleTarget res = null;
        ItemStack stack = router.peekBuffer(getItemsPerTick(router));
        switch (getDistributionStrategy()) {
            case ROUND_ROBIN:
                for (int i = 1; i <= nTargets; i++) {
                    nextTarget++;
                    if (nextTarget >= nTargets) nextTarget -= nTargets;
                    ModuleTarget tgt = getTargets().get(nextTarget);
                    if (okToInsert(tgt, stack)) {
                        res = tgt;
                        break;
                    }
                }
                break;
            case RANDOM:
                int r = router.nonNullLevel().random.nextInt(getTargets().size());
                res = getTargets().get(r);
                break;
            case NEAREST_FIRST:
                for (ModuleTarget tgt : getTargets()) {
                    if (okToInsert(tgt, stack)) {
                        res = tgt;
                        break;
                    }
                }
                break;
            case FURTHEST_FIRST:
                for (int i = getTargets().size() - 1; i >= 0; i--) {
                    if (okToInsert(getTargets().get(i), stack)) {
                        res = getTargets().get(i);
                        break;
                    }
                }
                break;
        }

        return res;
    }

    private boolean okToInsert(ModuleTarget target, ItemStack stack) {
        return target.getItemHandler().map(h -> ItemHandlerHelper.insertItem(h, stack, true).isEmpty()).orElse(false);
    }

    public enum DistributionStrategy implements TranslatableEnum, StringRepresentable {
        ROUND_ROBIN("round_robin"),
        RANDOM("random"),
        NEAREST_FIRST("nearest_first"),
        FURTHEST_FIRST("furthest_first");

        private final String name;

        DistributionStrategy(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.itemText.distributor.strategy." + name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record DistributorSettings(DistributionStrategy strategy, TransferDirection direction) {
        public static final DistributorSettings DEFAULT = new DistributorSettings(DistributionStrategy.ROUND_ROBIN, TransferDirection.FROM_ROUTER);

        public static final Codec<DistributorSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                StringRepresentable.fromEnum(DistributionStrategy::values)
                        .optionalFieldOf("strategy", DistributionStrategy.ROUND_ROBIN)
                        .forGetter(DistributorSettings::strategy),
                StringRepresentable.fromEnum(TransferDirection::values)
                        .optionalFieldOf("pulling", TransferDirection.TO_ROUTER)
                        .forGetter(DistributorSettings::direction)
        ).apply(builder, DistributorSettings::new));

        public static final StreamCodec<FriendlyByteBuf,DistributorSettings> STREAM_CODEC = StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(DistributionStrategy.class), DistributorSettings::strategy,
                NeoForgeStreamCodecs.enumCodec(TransferDirection.class), DistributorSettings::direction,
                DistributorSettings::new
        );

        public boolean isPulling() {
            return direction == TransferDirection.TO_ROUTER;
        }
    }
}
