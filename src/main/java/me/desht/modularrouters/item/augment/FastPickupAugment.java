package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;

public class FastPickupAugment extends Augment {
    @Override
    public boolean isCompatible(ItemModule.ModuleType moduleType) {
        return moduleType == ItemModule.ModuleType.VACUUM;
    }
}
