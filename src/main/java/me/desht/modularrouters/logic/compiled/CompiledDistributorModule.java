package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.BeamData;
import me.desht.modularrouters.util.TranslatableEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class CompiledDistributorModule extends CompiledSenderModule2 {
    public static final String NBT_STRATEGY = "DistStrategy";
    public static final String NBT_PULLING = "Pulling";

    public enum DistributionStrategy implements TranslatableEnum {
        ROUND_ROBIN,
        RANDOM,
        NEAREST_FIRST,
        FURTHEST_FIRST;

        @Override
        public String getTranslationKey() {
            return "modularrouters.itemText.distributor.strategy." + this;
        }
    }

    private final DistributionStrategy distributionStrategy;
    private int nextTarget = 0;
    private boolean pulling = false;

    public CompiledDistributorModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        CompoundTag compound = stack.getTagElement(ModularRouters.MODID);
        if (compound != null) {
            distributionStrategy = DistributionStrategy.values()[compound.getInt(NBT_STRATEGY)];
            if (distributionStrategy == DistributionStrategy.FURTHEST_FIRST) {
                nextTarget = getTargets().size() - 1;
            }
            pulling = compound.getBoolean(NBT_PULLING);
        } else {
            distributionStrategy = DistributionStrategy.ROUND_ROBIN;
        }
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        return pulling ? executePull(router) : super.execute(router);
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
        return pulling;
    }

    public DistributionStrategy getDistributionStrategy() {
        return distributionStrategy;
    }

    @Override
    void playParticles(ModularRouterBlockEntity router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
            BeamData data = new BeamData(router.getTickRate(), targetPos, stack, getBeamColor());
            router.addItemBeam(isPulling() ? data.reverseItems() : data);
        }
    }

    @Override
    protected int getBeamColor() {
        return isPulling() ? 0x6080FF :  super.getBeamColor();
    }

    @Override
    protected List<ModuleTarget> setupTargets(ModularRouterBlockEntity router, ItemStack stack) {
        Set<ModuleTarget> t = TargetedModule.getTargets(stack, router != null && !router.nonNullLevel().isClientSide);
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
        if (nTargets == 1) return getTargets().get(0); // degenerate case

        ModuleTarget res = null;
        ItemStack stack = router.peekBuffer(getItemsPerTick(router));
        switch (distributionStrategy) {
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
}
