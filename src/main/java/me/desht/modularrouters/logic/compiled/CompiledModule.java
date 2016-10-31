package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.CountedItemStacks;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class CompiledModule {
    private final Filter filter;
    private final Module module;
    private final Module.RelativeDirection direction;
    private final ModuleTarget target;
    private final RouterRedstoneBehaviour behaviour;
    private final boolean termination;
    private final EnumFacing facing;
    private final int regulationAmount;

    private int lastMatchPos = 0;

    public CompiledModule(TileEntityItemRouter router, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemModule)) {
            throw new IllegalArgumentException("expected module router module, got " + stack);
        }

        module = ItemModule.getModule(stack);
        direction = ModuleHelper.getDirectionFromNBT(stack);
        target = setupTarget(router, stack);
        filter = new Filter(target, stack);
        termination = ModuleHelper.terminates(stack);
        behaviour = ModuleHelper.getRedstoneBehaviour(stack);
        regulationAmount = ModuleHelper.getRegulatorAmount(stack);
        facing = router == null ? null : router.getAbsoluteFacing(direction);
    }

    public abstract boolean execute(TileEntityItemRouter router);

    public Module getModule() {
        return module;
    }

    public Filter getFilter() {
        return filter;
    }

    public Module.RelativeDirection getDirection() {
        return direction;
    }

    public ModuleTarget getTarget() {
        return target;
    }

    public boolean termination() {
        return termination;
    }

    public RouterRedstoneBehaviour getRedstoneBehaviour() {
        return behaviour;
    }

    int getRegulationAmount() {
        return regulationAmount;
    }

    EnumFacing getFacing() {
        return facing;
    }

    public void onCompiled(TileEntityItemRouter router) {
        if (behaviour == RouterRedstoneBehaviour.PULSE) {
            router.setHasPulsedModules(true);
        }
    }

    public void cleanup(TileEntityItemRouter router) {
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
    int getLastMatchPos(int offset, int size) {
        int pos = lastMatchPos + offset;
        if (pos >= size) pos -= size;
        return pos;
    }

    /**
     * Store the last position where we found a match.
     *
     * @param lastMatchPos last matched position
     */
    void setLastMatchPos(int lastMatchPos) {
        this.lastMatchPos = lastMatchPos;
    }

    /**
     * Default target for this module is the block adjacent to the router, in the module's
     * configured direction.  Can be overridden, though.
     *
     * @param router router in which the module is installed
     * @param stack the module itemstack
     * @return a router target object
     */
    protected ModuleTarget setupTarget(TileEntityItemRouter router, ItemStack stack) {
        if (router == null) {
            return null;
        }
        EnumFacing facing = router.getAbsoluteFacing(direction);
        return new ModuleTarget(router.getWorld().provider.getDimension(), router.getPos().offset(facing), facing.getOpposite());
    }

    /**
     * Try to transfer some items from the given ItemHandler to the given router.  The number of
     * items attempted depends on the router's stack upgrades.
     *
     * @param handler the item handler
     * @param router  the router
     * @return number of items actually transferred
     */
    int transferToRouter(IItemHandler handler, TileEntityItemRouter router) {
        CountedItemStacks count = null;
        if (getRegulationAmount() > 0) {
            count = new CountedItemStacks(handler);
        }

        ItemStack wanted = findItemToPull(router, handler, router.getItemsPerTick(), count);
        if (wanted == null) {
            return 0;
        }

        if (count != null) {
            wanted.stackSize = Math.min(wanted.stackSize, count.getOrDefault(wanted, 0) - getRegulationAmount());
            if (wanted.stackSize <= 0) {
                return 0;
            }
        }

        int totalInserted = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            int pos = getLastMatchPos(i, handler.getSlots());
            ItemStack toPull = handler.extractItem(pos, wanted.stackSize, true);
            if (ItemHandlerHelper.canItemStacksStack(wanted, toPull)) {
                // this item is suitable for pulling
                ItemStack notInserted = router.insertBuffer(toPull);
                int inserted = toPull.stackSize - (notInserted == null ? 0 : notInserted.stackSize);
                handler.extractItem(pos, inserted, false);
                wanted.stackSize -= inserted;
                totalInserted += inserted;
                if (wanted.stackSize <= 0 || router.isBufferFull()) {
                    setLastMatchPos(pos);
                    return totalInserted;
                }
            }
        }
        return totalInserted;
    }

    private ItemStack findItemToPull(TileEntityItemRouter router, IItemHandler handler, int nToTake, CountedItemStacks count) {
        ItemStack stackInRouter = router.peekBuffer(1);
        ItemStack result = null;
        if (stackInRouter != null && getFilter().pass(stackInRouter)) {
            // something in the router - try to pull more of that
            result = stackInRouter.copy();
            result.stackSize = nToTake;
        } else if (stackInRouter == null) {
            // router empty - just pull the next item that passes the filter
            for (int i = 0; i < handler.getSlots(); i++) {
                int pos = getLastMatchPos(i, handler.getSlots());
                ItemStack stack = handler.getStackInSlot(pos);
                if (stack != null && getFilter().pass(stack) && (count == null || count.get(stack) > getRegulationAmount())) {
                    setLastMatchPos(pos);
                    result = stack.copy();
                    result.stackSize = nToTake;
                }
            }
        }
        return result;
    }
}
