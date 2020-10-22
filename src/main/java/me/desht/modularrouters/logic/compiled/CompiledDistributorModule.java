package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class CompiledDistributorModule extends CompiledSenderModule2 {
    public static final String NBT_STRATEGY = "DistStrategy";

    public enum DistributionStrategy {
        ROUND_ROBIN,
        RANDOM,
        NEAREST_FIRST,
        FURTHEST_FIRST;

        public String getTranslationKey() {
            return "modularrouters.itemText.distributor.strategy." + this.toString();
        }
    }

    private final DistributionStrategy distributionStrategy;
    private int nextTarget = 0;

    public CompiledDistributorModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        CompoundNBT compound = stack.getChildTag(ModularRouters.MODID);
        if (compound != null) {
            distributionStrategy = DistributionStrategy.values()[compound.getInt(NBT_STRATEGY)];
            if (distributionStrategy == DistributionStrategy.FURTHEST_FIRST) {
                nextTarget = getTargets().size() - 1;
            }
        } else {
            distributionStrategy = DistributionStrategy.ROUND_ROBIN;
        }
    }

    public DistributionStrategy getDistributionStrategy() {
        return distributionStrategy;
    }

    @Override
    protected List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        Set<ModuleTarget> t = TargetedModule.getTargets(stack, router != null && !router.getWorld().isRemote);
        List<ModuleTarget> l = Lists.newArrayList(t);
        if (router == null) return l;
        l.sort(Comparator.comparingDouble(o -> calcDist(o, router)));
        return l;
    }

    private static double calcDist(ModuleTarget tgt, TileEntity te) {
        double distance = tgt.gPos.getPos().distanceSq(te.getPos());
        if (!tgt.isSameWorld(te.getWorld())) {
            distance += 100000000;  // cross-dimension penalty
        }
        return distance;
    }

    @Override
    public ModuleTarget getEffectiveTarget(TileEntityItemRouter router) {
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
                int r = router.getWorld().rand.nextInt(getTargets().size());
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
