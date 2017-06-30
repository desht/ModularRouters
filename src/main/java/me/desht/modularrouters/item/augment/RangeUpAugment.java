package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ItemModule;

public class RangeUpAugment extends Augment {
    @Override
    public boolean isCompatible(ItemModule.ModuleType moduleType) {
        return ItemModule.getModule(moduleType) instanceof IRangedModule;
    }
}
