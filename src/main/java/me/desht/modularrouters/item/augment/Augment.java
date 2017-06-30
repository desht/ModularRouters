package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.ItemSubTypes;
import me.desht.modularrouters.item.module.ItemModule;

public abstract class Augment extends ItemSubTypes.SubItemHandler {
    public static final int SLOTS = 4;

    public abstract boolean isCompatible(ItemModule.ModuleType moduleType);
}
