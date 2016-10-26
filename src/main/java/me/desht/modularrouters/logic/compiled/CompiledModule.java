package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.logic.RouterTarget;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

public abstract class CompiledModule {
    private final Filter filter;
    private final Module module;
    private final Module.RelativeDirection direction;
    private final RouterTarget target;
    private final RouterRedstoneBehaviour behaviour;
    private final boolean termination;
    private final EnumFacing facing;

    private int lastMatchPos = 0;

    public CompiledModule(TileEntityItemRouter router, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemModule)) {
            throw new IllegalArgumentException("expected module router module, got " + stack);
        }

        module = ItemModule.getModule(stack);
        direction = module.getDirectionFromNBT(stack);
        target = setupTarget(router, stack);
        filter = new Filter(target, stack);
        termination = module.terminates(stack);
        behaviour = module.getRedstoneBehaviour(stack);
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

    public RouterTarget getTarget() {
        return target;
    }

    public boolean termination() {
        return termination;
    }

    public RouterRedstoneBehaviour getRedstoneBehaviour() {
        return behaviour;
    }

    public EnumFacing getFacing() {
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

    public int getLastMatchPos() {
        return lastMatchPos;
    }

    /**
     * Get the last position where we found a match.  Caching this can help reduce the amount of inventory searching
     * needed for some modules.
     *
     * @param offset offset from the last saved position
     * @param size size of the inventory being searched
     * @return the last position including offset, and wrapped to start of inventory if necessary
     */
    public int getLastMatchPos(int offset, int size) {
        int pos = lastMatchPos + offset;
        if (pos >= size) pos -= size;
        return pos;
    }

    /**
     * Store the last position where we found a match.
     *
     * @param lastMatchPos last matched position
     */
    public void setLastMatchPos(int lastMatchPos) {
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
    protected RouterTarget setupTarget(TileEntityItemRouter router, ItemStack stack) {
        if (router == null) {
            return null;
        }
        EnumFacing facing = router.getAbsoluteFacing(direction);
        return new RouterTarget(router.getWorld().provider.getDimension(), router.getPos().offset(facing), facing.getOpposite());
    }

    /**
     * Try to transfer some items from the given ItemHandler to the given router.  The number of
     * items attempted depends on the router's stack upgrades.
     *
     * @param handler the item handler
     * @param router the router
     * @return number of items actually transferred
     */
    protected int transferItems(IItemHandler handler, TileEntityItemRouter router) {
        int toTake = router.getItemsPerTick();
        for (int i = 0; i < handler.getSlots(); i++) {
            int pos = getLastMatchPos(i, handler.getSlots());
            ItemStack toExtract = handler.extractItem(pos, toTake, true);
            if (toExtract != null && getFilter().pass(toExtract)) {
                ItemStack notInserted = router.getBuffer().insertItem(0, toExtract, false);
                int inserted = toExtract.stackSize - (notInserted == null ? 0 : notInserted.stackSize);
                handler.extractItem(pos, inserted, false);
                toTake -= inserted;
                if (toTake <= 0 || router.isBufferFull()) {
                    setLastMatchPos(pos);
                    return inserted;
                }
            }
        }
        return router.getItemsPerTick() - toTake;
    }
}
