package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
        FURTHEST_FIRST
    }

    private final DistributionStrategy distributionStrategy;
    private int nextTarget = 0;

    public CompiledDistributorModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null) {
            distributionStrategy = DistributionStrategy.values()[compound.getInteger(NBT_STRATEGY)];
            if (distributionStrategy == DistributionStrategy.FURTHEST_FIRST) {
                nextTarget = getTargets().size() - 1;
            }
        } else {
            distributionStrategy = DistributionStrategy.ROUND_ROBIN;
        }
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        return super.execute(router);
    }

    public DistributionStrategy getDistributionStrategy() {
        return distributionStrategy;
    }

    @Override
    protected List<ModuleTarget> setupTarget(TileEntityItemRouter router, ItemStack stack) {
        Set<ModuleTarget> t = TargetedModule.getTargets(stack, true);
        List<ModuleTarget> l = Lists.newArrayList(t);
        if (router == null) return l;
        l.sort(Comparator.comparingDouble(o -> calcDist(o, router)));
        return l;
    }

    private static double calcDist(ModuleTarget tgt, TileEntity te) {
        double distance = tgt.pos.getDistance(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
        if (tgt.dimId != te.getWorld().provider.getDimension()) {
            distance += 100000000;  // cross-dimension penalty
        }
        return distance;
    }

    @Override
    public ModuleTarget getActualTarget(TileEntityItemRouter router) {
        if (getTargets() == null || getTargets().isEmpty()) return null;

        ModuleTarget res = null;
        switch (distributionStrategy) {
            case ROUND_ROBIN:
                res = getTargets().get(nextTarget);
                nextTarget++;
                if (nextTarget >= getTargets().size()) {
                    nextTarget = 0;
                }
                break;
            case RANDOM:
                int r = ModularRouters.random.nextInt(getTargets().size());
                res = getTargets().get(r);
                break;
            case NEAREST_FIRST:
                ItemStack stack = router.peekBuffer(getItemsPerTick(router));
                for (ModuleTarget tgt : getTargets()) {
                    if (ItemHandlerHelper.insertItem(tgt.getItemHandler(), stack, true).isEmpty()) {
                        res = tgt;
                        break;
                    }
                }
                break;
            case FURTHEST_FIRST:
                ItemStack stack1 = router.peekBuffer(getItemsPerTick(router));
                for (int i = getTargets().size() - 1; i >= 0; i--) {
                    if (ItemHandlerHelper.insertItem(getTargets().get(i).getItemHandler(), stack1, true).isEmpty()) {
                        res = getTargets().get(i);
                        break;
                    }
                }
                break;
        }

        return res;
    }
}
