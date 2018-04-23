package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;

public class PushingAugment extends Augment {
    @Override
    public boolean isCompatible(ItemModule.ModuleType moduleType) {
        return moduleType == ItemModule.ModuleType.EXTRUDER || moduleType == ItemModule.ModuleType.EXTRUDER2;
    }
}
