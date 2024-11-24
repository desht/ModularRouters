package me.desht.modularrouters.api.event;

import me.desht.modularrouters.item.module.ModuleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired when a player attempts to add a new target to a module.
 * <p>
 * This event can be used to allow a module to target blocks it otherwise couldn't in conjunction with {@link ExecuteModuleEvent}.
 */
public final class AddModuleTargetEvent extends Event {
    private final ModuleItem item;
    private final UseOnContext context;
    private boolean valid;

    @ApiStatus.Internal
    public AddModuleTargetEvent(ModuleItem item, UseOnContext context, boolean valid) {
        this.item = item;
        this.context = context;
        this.valid = valid;
    }

    /**
     * {@return the module type}
     */
    public ModuleItem getModuleType() {
        return item;
    }

    /**
     * {@return the module stack}
     */
    public ItemStack getModule() {
        return context.getItemInHand();
    }

    /**
     * {@return the context of the interaction}
     */
    public UseOnContext getContext() {
        return context;
    }

    /**
     * {@return whether the targeted block is valid and may be selected}
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Making the target {@code valid} allows the player to select the block and the router to process it.
     * @see ExecuteModuleEvent
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
