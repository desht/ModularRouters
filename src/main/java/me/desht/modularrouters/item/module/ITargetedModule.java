package me.desht.modularrouters.item.module;

import com.google.common.collect.Sets;
import me.desht.modularrouters.api.event.AddModuleTargetEvent;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.ModuleTargetList;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ITargetedModule {
    /**
     * <strong>Override-only</strong> method that checks whether this module can have the block of the context selected.
     * <p>Note: it is not guaranteed that the module will only have blocks that pass this test selected, see {@link AddModuleTargetEvent}.
     */
    @ApiStatus.OverrideOnly
    default boolean isValidTarget(UseOnContext ctx) {
        return InventoryUtils.getInventory(ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace()).isPresent();
    }

    default int getMaxTargets() {
        return 1;
    }

    /**
     * Does this module have limited range?
     *
     * @return true if range is limited, false otherwise
     */
    default boolean isRangeLimited() {
        return true;
    }

    /**
     * {@return whether this module can operate in the given {@code dimension}}
     */
    default boolean canOperateInDimension(ResourceKey<Level> dimension) {
        return true;
    }

    /**
     * Retrieve multi-targeting information from a module itemstack.
     *
     * @param stack the module item stack
     * @param checkBlockName verify the name of the target block - only works server-side
     * @implNote the set will be limited to the max target amount of the module
     * @return a set of targets for the module
     */
    static Set<ModuleTarget> getTargets(ItemStack stack, boolean checkBlockName) {
        var max = ((ITargetedModule) stack.getItem()).getMaxTargets();
        Set<ModuleTarget> result = Sets.newHashSet();

        boolean update = false;
        var targets = stack.getOrDefault(ModDataComponents.MODULE_TARGET_LIST, ModuleTargetList.EMPTY).targets();
        for (int i = 0; i < targets.size() && result.size() < max; i++) {
            var target = targets.get(i);
            if (checkBlockName) {
                var newTarget = updateTargetBlockName(target);
                if (newTarget != target) update = true;
                target = newTarget;
            }
            if (target != null) {
                result.add(target);
            }
        }

        if (update) {
            setTargets(stack, result);
        }

        return result;
    }

    /**
     * Sets the targets of a module.
     * @param stack the module stack to update
     * @param targets the new targets
     */
    static void setTargets(ItemStack stack, Collection<ModuleTarget> targets) {
        stack.set(ModDataComponents.MODULE_TARGET_LIST, new ModuleTargetList(List.copyOf(targets)));
    }

    /**
     * Checks if the module can select the target of the {@code context}.
     */
    static boolean canSelectTarget(UseOnContext context) {
        var module = context.getItemInHand().getItem();
        return NeoForge.EVENT_BUS.post(new AddModuleTargetEvent((ModuleItem) module, context, ((ITargetedModule) module).isValidTarget(context))).isValid();
    }

    private static ModuleTarget updateTargetBlockName(ModuleTarget target) {
        ServerLevel level = MiscUtil.getWorldForGlobalPos(target.gPos);
        BlockPos pos = target.gPos.pos();
        if (level != null && level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            String invName = BlockUtil.getBlockName(level, pos);
            if (!target.blockTranslationKey.equals(invName)) {
                return new ModuleTarget(target.gPos, target.face, invName);
            } else {
                return target;
            }
        }
        return null;
    }
}
