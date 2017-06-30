package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;

public class RedstoneAugment extends Augment {
    @Override
    public boolean isCompatible(ItemModule.ModuleType moduleType) {
        return true;
    }
}
