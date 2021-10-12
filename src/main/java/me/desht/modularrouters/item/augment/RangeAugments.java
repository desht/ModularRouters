package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ModuleItem;

public abstract class RangeAugments extends AugmentItem {
    public static class RangeUpAugment extends RangeAugments {
        @Override
        public int getMaxAugments(ModuleItem moduleType) {
            return moduleType instanceof IRangedModule r ? r.getHardMaxRange() - r.getBaseRange() : 0;
        }
    }

    public static class RangeDownAugment extends RangeAugments {
        @Override
        public int getMaxAugments(ModuleItem moduleType) {
            return moduleType instanceof IRangedModule r ? r.getBaseRange() - 1 : 0;
        }
    }
}
