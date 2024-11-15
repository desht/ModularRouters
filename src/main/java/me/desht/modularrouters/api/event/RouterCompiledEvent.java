package me.desht.modularrouters.api.event;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Called when a router is (re)compiled. Can be used to update state that's
 * independent of an upgrade or module and must be refreshed even when the upgrade isn't present.
 *
 * @see Upgrades
 * @see Modules
 */
public abstract sealed class RouterCompiledEvent extends Event {
    private final ModularRouterBlockEntity router;

    @ApiStatus.Internal
    public RouterCompiledEvent(ModularRouterBlockEntity router) {
        this.router = router;
    }

    /**
     * {@return the router that is being compiled}
     */
    public ModularRouterBlockEntity getRouter() {
        return router;
    }

    /**
     * Called when a router (re)compiles its upgrades. This <strong>will</strong> be called on
     * the client-side too when the GUI is opened.
     */
    public final static class Upgrades extends RouterCompiledEvent {
        @ApiStatus.Internal
        public Upgrades(ModularRouterBlockEntity router) {
            super(router);
        }
    }

    /**
     * Called when a router (re)compiles its modules.
     */
    public final static class Modules extends RouterCompiledEvent {
        public Modules(ModularRouterBlockEntity router) {
            super(router);
        }
    }
}
