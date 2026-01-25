package me.desht.modularrouters.api.matching;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;

/**
 * Base item and fluid matcher
 */
public interface IItemMatcher {
    /**
     * Check if the given stack matches. The module flags are passed from the module's current settings; they may
     * be ignored, depending on the filter (e.g. it may have its own system for determining what should be matched).
     *
     * @param stack          the stack to test
     * @param flags          the current matching flags for the module
     * @param registryAccess
     * @return true if the item matches, false otherwise
     */
    boolean matchItem(ItemStack stack, IModuleFlags flags, @Nullable HolderLookup.Provider registryAccess);

    /**
     * Check if the given fluid matches. Since Modular Routers doesn't contain fluids directly, this is intended
     * to work on fluid-containing items. By default, this always returns false, but Fluid Modules provide an item
     * matcher which does check for contained fluids.
     *
     * @param fluid          the fluid to test
     * @param flags          the current matching flags for the module
     * @param registryAccess
     * @return true if the fluid matches, false otherwise
     */
    default boolean matchFluid(Fluid fluid, IModuleFlags flags, @Nullable HolderLookup.Provider registryAccess) {
        return false;
    }
}
