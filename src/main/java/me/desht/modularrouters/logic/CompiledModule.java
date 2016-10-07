package me.desht.modularrouters.logic;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.item.ItemStack;

public class CompiledModule {
    private final Filter filter;
    private final Module module;
    private final Module.RelativeDirection direction;
    private final RouterTarget target;
    private final RouterRedstoneBehaviour behaviour;
    private final boolean termination;

    private int lastMatchPos = 0;

    public CompiledModule(TileEntityItemRouter router, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemModule)) {
            throw new IllegalArgumentException("expected module router module, got " + stack);
        }

        filter = new Filter(stack);
        module = ItemModule.getModule(stack);
        direction = module.getDirectionFromNBT(stack);
        termination = module.terminates(stack);
        target = module.getTarget(router, stack);
        behaviour = module.getRedstoneBehaviour(stack);
    }

    public Module getModule() {
        return module;
    }

    public Filter getFilter() {
        return filter;
    }

    public Module.RelativeDirection getDirection() {
        return direction;
    }

    public boolean execute(TileEntityItemRouter router) {
        return module.execute(router, this);
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
     * needed.
     *
     * @param offset offset from the last saved position
     * @param size size of the inventory being searched
     * @return the last position including offset, and wrapper to start of inventory if necessary
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
}
