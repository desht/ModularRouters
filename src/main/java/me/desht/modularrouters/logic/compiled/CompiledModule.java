package me.desht.modularrouters.logic.compiled;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.desht.modularrouters.api.event.ExecuteModuleEvent;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.AugmentItem.AugmentCounter;
import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.logic.settings.ModuleSettings;
import me.desht.modularrouters.logic.settings.ModuleTermination;
import me.desht.modularrouters.logic.settings.RedstoneBehaviour;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.CountedItemStacks;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class CompiledModule {
    private final ModuleItem module;
    protected final ModuleSettings commonSettings;
    private final Filter filter;
    @Nullable
    private final Direction absoluteFacing;
    private final List<ModuleTarget> targets;
    @Nullable
    private final Direction routerFacing;
    private final AugmentCounter augmentCounter;
    private final int range, rangeSquared;
    @Nullable
    private final ExecuteModuleEvent event;

    private int lastMatchPos = 0;
    private final Map<BlockPos,Integer> lastMatchPosMap = new Object2IntOpenHashMap<>();

    /**
     * Base constructor for compiled modules.  This can be called for both installed and uninstalled modules;
     * when the module is not installed in a router, null can be passed, so any subclass constructors must be able to
     * handle a null router.
     *
     * @param router router the module is installed in, may be null for an uninstalled module
     * @param stack item stack of the module item being compiled
     */
    protected CompiledModule(@Nullable ModularRouterBlockEntity router, ItemStack stack) {
        Validate.isTrue(stack.getItem() instanceof ModuleItem, "expected module item, got " + stack);

        module = (ModuleItem) stack.getItem();
        augmentCounter = new AugmentCounter(stack);
        commonSettings = ModuleItem.getCommonSettings(stack);
        range = module instanceof IRangedModule r ? r.getCurrentRange(getRangeModifier()) : 0;
        rangeSquared = range * range;
        targets = setupTargets(router, stack);
        filter = new Filter(stack, shouldStoreRawFilterItems(), augmentCounter.getAugmentCount(ModItems.FILTER_ROUND_ROBIN_AUGMENT.get()) > 0);
        absoluteFacing = router == null ? null : router.getAbsoluteFacing(commonSettings.facing());
        routerFacing = router == null ? null : router.getAbsoluteFacing(RelativeDirection.FRONT);
        event = router == null ? null : new ExecuteModuleEvent(router, this);
    }

    /**
     * Execute this installed module.  When this is called, the router has already verified that the module has a
     * valid target, i.e. getTarget() will not return null.  This should only be called by the router.
     *
     * @param router router the module is installed in, may <strong>not</strong> be null
     * @return true if the module did some work, false otherwise
     */
    public abstract boolean execute(@Nonnull ModularRouterBlockEntity router);

    @Nonnull
    public Filter getFilter() {
        return filter;
    }

    public final RelativeDirection getDirection() {
        return commonSettings.facing();
    }

    protected boolean shouldStoreRawFilterItems() {
        return false;
    }

    /**
     * Get the static target for a router module.  This is set up when the router is compiled, and does not
     * necessarily reflect the true target for all modules.  Use {@link #getEffectiveTarget(ModularRouterBlockEntity)}
     * to get the actual target that should be interacted with when executing the module.
     *
     * @return the first target as set up by {@link #setupTargets(ModularRouterBlockEntity, ItemStack)}
     */
    public ModuleTarget getTarget() {
        return targets == null || targets.isEmpty() ? null : targets.getFirst();
    }

    /**
     * Used by modules which can store multiple targets, e.g. the distributor module.  See also
     * {@link #getTarget()}.
     *
     * @return a list of the defined targets as set up by {@link #setupTargets(ModularRouterBlockEntity, ItemStack)}
     */
    public List<ModuleTarget> getTargets() {
        return targets;
    }

    public boolean hasTarget() { return targets != null && !targets.isEmpty(); }

    public ModuleTermination termination() {
        return commonSettings.termination();
    }

    public final RedstoneBehaviour getRedstoneBehaviour() {
        return commonSettings.redstoneBehaviour();
    }

    public int getRegulationAmount() {
        return augmentCounter.getAugmentCount(ModItems.REGULATOR_AUGMENT.get()) > 0 ? commonSettings.regulatorAmount() : 0;
    }

    public int getAugmentCount(Supplier<Item> augmentType) {
        return augmentCounter.getAugmentCount(augmentType);
    }

    public int getAugmentCount(Item augmentType) {
        return augmentCounter.getAugmentCount(augmentType);
    }

    /**
     * Get the absolute direction this module faces, based on its relative direction and the facing of the router
     * in which it's installed.  This will be null if the module isn't installed in a router, or the router's facing
     * direction if the module's relative direction is NONE.
     *
     * @return absolute direction of the module
     */
    @Nullable
    public final Direction getAbsoluteFacing() {
        return absoluteFacing;
    }

    /**
     * Called immediately after the router compiles or recompiles the module, which happens if a change to any
     * installed module item occurs (not necessarily this one). Can be overridden in subclasses to carry out any
     * initialisation work needed, but be sure to call this super implementation!
     *
     * @param router the router this module is installed in
     */
    @ApiStatus.OverrideOnly
    public void onCompiled(@NotNull ModularRouterBlockEntity router) {
        if (getRedstoneBehaviour() == RedstoneBehaviour.PULSE) {
            router.setHasPulsedModules(true);
        }
    }

    /**
     * Called immediately before the router compiles or recompiles the module, which happens if a change to any
     * installed module item occurs (not necessarily this one). Can be overridden in subclasses to carry out any
     * cleanup work needed, but be sure to call this super implementation!
     *
     * @param router the router this module is installed in
     */
    @ApiStatus.OverrideOnly
    public void cleanup(ModularRouterBlockEntity router) {
        // does nothing by default
    }

    /**
     * Get the last position where we found a match.  Caching this can help reduce the amount of inventory searching
     * needed for some modules.
     *
     * @param offset offset from the last saved position
     * @param size size of the inventory being searched
     * @return the last position including offset, and wrapped to start of inventory if necessary
     */
    private int getLastMatchPos(BlockPos key, int offset, int size) {
        int pos = (key == null ? lastMatchPos : lastMatchPosMap.getOrDefault(key, 0)) + offset;
        while (pos >= size) pos -= size;
        return pos;
    }

    /**
     * Store the last position where we found a match.
     *
     * @param lastMatchPos last matched position
     */
    private void setLastMatchPos(BlockPos key, int lastMatchPos) {
        if (key == null)
            this.lastMatchPos = lastMatchPos;
        else
            lastMatchPosMap.put(key, lastMatchPos);
    }

    /**
     * Default target for this module is the block adjacent to the router, in the module's
     * configured direction.  Can be overridden by submodules.
     *
     * @param router router in which the module is installed
     * @param stack the module itemstack
     * @return a list of router target objects (for most modules this is a singleton list)
     */
    protected List<ModuleTarget> setupTargets(ModularRouterBlockEntity router, ItemStack stack) {
        if (router == null || (module.isDirectional() && getDirection() == RelativeDirection.NONE)) {
            return null;
        }
        Direction facing = router.getAbsoluteFacing(getDirection());
        BlockPos pos = router.getBlockPos().relative(facing);
        String blockName = BlockUtil.getBlockName(router.getLevel(), pos);
        GlobalPos gPos = MiscUtil.makeGlobalPos(router.nonNullLevel(), pos);
        return List.of(new ModuleTarget(gPos, facing.getOpposite(), blockName));
    }

    public int getItemsPerTick(ModularRouterBlockEntity router) {
        int n = augmentCounter.getAugmentCount(ModItems.STACK_AUGMENT.get());
        return n > 0 ? Math.min(1 << n, 64) : router.getItemsPerTick();
    }

    /**
     * Try to transfer some items from the given ItemHandler to the given router.  The number of
     * items attempted depends on the router's stack upgrades.
     *
     * @param handler the item handler
     * @param key the handler's position, may be null - used as a key for search pos caching
     * @param router the router
     * @return items actually transferred
     */
    public final ItemStack transferToRouter(IItemHandler handler, @Nullable BlockPos key, ModularRouterBlockEntity router) {
        CountedItemStacks count = getRegulationAmount() > 0 ? new CountedItemStacks(handler) : null;

        ItemStack wanted = findItemToPull(router, handler, key, getItemsPerTick(router), count);
        if (wanted.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack transferred = ItemStack.EMPTY;
        for (int i = 0; i < handler.getSlots(); i++) {
            int slot = getLastMatchPos(key, i, handler.getSlots());
            ItemStack toPull = handler.extractItem(slot, wanted.getCount(), true);
            if (toPull.isEmpty()) {
                // we'd found an item to pull, but it looks like this handler doesn't allow us to extract it
                // give up, but advance the last match pos, so we don't get stuck trying this slot forever
                setLastMatchPos(key, (slot + 1) % handler.getSlots());
                return transferred;
            }
            if (ItemStack.isSameItemSameComponents(wanted, toPull)) {
                // this item is suitable for pulling
                ItemStack notInserted = router.insertBuffer(toPull);
                int inserted = toPull.getCount() - notInserted.getCount();
                transferred = handler.extractItem(slot, inserted, false);
                wanted.shrink(inserted);
                if (wanted.isEmpty() || router.isBufferFull()) {
                    if (handler.getSlots() > 0) {
                        setLastMatchPos(key, handler.getStackInSlot(slot).isEmpty() ? (slot + 1) % handler.getSlots() : slot);
                    }
                    return transferred;
                }
            }
        }
        return transferred;
    }

    private ItemStack findItemToPull(ModularRouterBlockEntity router, IItemHandler handler, BlockPos key, int nToTake, CountedItemStacks count) {
        ItemStack stackInRouter = router.peekBuffer(1);
        if (!stackInRouter.isEmpty() && getFilter().test(stackInRouter) && (count == null || count.getInt(stackInRouter) - nToTake >= getRegulationAmount())) {
            // something in the router - try to pull more of that
            return stackInRouter.copyWithCount(nToTake);
        } else if (stackInRouter.isEmpty()) {
            // router empty - just pull the next item that passes the filter
            for (int i = 0; i < handler.getSlots(); i++) {
                int pos = getLastMatchPos(key, i, handler.getSlots());
                ItemStack stack = handler.getStackInSlot(pos);
                if (getFilter().test(stack) && (count == null || count.getInt(stack) - nToTake >= getRegulationAmount())) {
                    setLastMatchPos(key, pos);
                    return stack.copyWithCount(nToTake);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Get the effective target for this module, which is not necessarily the same as the result of getTarget().
     * E.g. for a Sender Mk1, the real target may be a few blocks away, and may change without router recompilation
     * if blocks are placed or removed.
     *
     * @return the real target for this module
     */
    public ModuleTarget getEffectiveTarget(ModularRouterBlockEntity router) {
        return getTarget();
    }

    @ApiStatus.OverrideOnly
    public boolean shouldRun(boolean powered, boolean pulsed) {
        return getRedstoneBehaviour().shouldRun(powered, pulsed);
    }

    public boolean isRegulationOK(ModularRouterBlockEntity router, boolean inbound) {
        if (commonSettings.regulatorAmount() == 0) return true; // no regulation
        int items = router.getBufferItemStack().getCount();
        return inbound && commonSettings.regulatorAmount() > items || !inbound && commonSettings.regulatorAmount() < items;
    }

    public int getRange() {
        return range;
    }

    public int getRangeSquared() {
        return rangeSquared;
    }

    private int getRangeModifier() {
        return getAugmentCount(ModItems.RANGE_UP_AUGMENT) - getAugmentCount(ModItems.RANGE_DOWN_AUGMENT);
    }

    @Nullable
    protected Direction getRouterFacing() {
        return routerFacing;
    }

    @ApiStatus.OverrideOnly
    public void onNeighbourChange(ModularRouterBlockEntity router) {
    }

    public int getEnergyCost() {
        return module.getEnergyCost(ItemStack.EMPTY);
    }

    public boolean careAboutItemAttributes() {
        return false;
    }

    /**
     * {@return the module type}
     */
    public ModuleItem getModule() {
        return module;
    }

    @Nullable
    @ApiStatus.Internal
    public ExecuteModuleEvent getEvent() {
        return event;
    }
}
