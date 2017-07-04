package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ItemModule;

public abstract class RangeAugments extends Augment {
    @Override
    public boolean isCompatible(ItemModule.ModuleType moduleType) {
        return ItemModule.getModule(moduleType) instanceof IRangedModule;
    }

    static class RangeUpAugment extends RangeAugments {
    }

    static class RangeDownAugment extends RangeAugments {
    }
}
