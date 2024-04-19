package me.desht.modularrouters.api.event;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Fired to register {@link DataSlot data slots} for the router menu. <br>
 * Use this to sync data to display in the GUI.
 */
public class RegisterRouterContainerData extends Event {
    private final ModularRouterBlockEntity router;
    private final Map<ResourceLocation, DataSlot> data = new HashMap<>();

    @ApiStatus.Internal
    public RegisterRouterContainerData(ModularRouterBlockEntity router) {
        this.router = router;
    }

    /**
     * Register a {@link DataSlot data slot}.
     *
     * @param id       the ID of the slot
     * @param dataSlot the slot to register
     */
    public void register(ResourceLocation id, DataSlot dataSlot) {
        data.put(id, dataSlot);
    }

    /**
     * {@return the router of the menu}
     */
    public ModularRouterBlockEntity getRouter() {
        return router;
    }

    @ApiStatus.Internal
    public Map<ResourceLocation, DataSlot> getData() {
        return Collections.unmodifiableMap(data);
    }
}
