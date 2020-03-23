package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ItemModule;

public abstract class RangeAugments extends ItemAugment {
    @Override
    public boolean isCompatible(ItemModule moduleType) {
        return moduleType instanceof IRangedModule;
    }

    public static class RangeUpAugment extends RangeAugments {
    }

    public static class RangeDownAugment extends RangeAugments {
    }
}
