package me.desht.modularrouters.api.event;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Called when a router {@link me.desht.modularrouters.logic.compiled.CompiledModule#execute(ModularRouterBlockEntity) executes} a module.
 */
public class ExecuteModuleEvent extends Event implements ICancellableEvent {
    private boolean executed;
    private final ModularRouterBlockEntity router;
    private final CompiledModule module;

    public ExecuteModuleEvent(ModularRouterBlockEntity router, CompiledModule module) {
        this.router = router;
        this.module = module;
    }

    /**
     * {@return the router executing the module}
     */
    public ModularRouterBlockEntity getRouter() {
        return router;
    }

    /**
     * {@return the executed module}
     */
    public CompiledModule getModule() {
        return module;
    }

    /**
     * If set to {@code true}, the router will consider the module executed.
     *
     * @apiNote to prevent the module itself from being executed, you need to {@link #setCanceled(boolean) cancel} the event too
     */
    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    /**
     * {@return whether the router should consider the module executed}
     */
    public boolean isExecuted() {
        return this.executed;
    }

    /**
     * Cancel this event to prevent the module from executing.
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }
}
