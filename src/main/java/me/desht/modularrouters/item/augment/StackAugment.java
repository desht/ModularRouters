package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;

public class StackAugment extends Augment {
    @Override
    public boolean isCompatible(ItemModule.ModuleType moduleType) {
        return moduleType != ItemModule.ModuleType.DETECTOR
                && moduleType != ItemModule.ModuleType.EXTRUDER
                && moduleType != ItemModule.ModuleType.EXTRUDER2
                && moduleType != ItemModule.ModuleType.BREAKER
                && moduleType != ItemModule.ModuleType.PLACER
                && moduleType != ItemModule.ModuleType.FLUID;
    }
}
