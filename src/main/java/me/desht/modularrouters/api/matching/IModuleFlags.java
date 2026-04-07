package me.desht.modularrouters.api.matching;

/**
 * Represents the current filtering flags that a module has.
 */
public interface IModuleFlags {
    /**
     * {@return true if this module is in whitelist mode, false if in blacklist mode}
     */
    boolean whiteList();

    /**
     * {@return true if this module matches item durability}
     */
    boolean matchDamage();

    /**
     * {@return true if this module matches item data components (including durability}
     */
    boolean matchComponents();

    /**
     * {@return true if this module matches by item tag}
     */
    boolean matchItemTags();

    /**
     * {@return true if all items/filters in the module's filter slots must match}
     */
    boolean matchAllItems();
}
