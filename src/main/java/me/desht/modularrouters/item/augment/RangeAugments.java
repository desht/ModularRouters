package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ModuleItem;

public abstract class RangeAugments extends AugmentItem {
    public static class RangeUpAugment extends RangeAugments {
        @Override
        public int getMaxAugments(ModuleItem moduleType) {
            if (moduleType instanceof IRangedModule) {
                IRangedModule r = (IRangedModule) moduleType;
                return r.getHardMaxRange() - r.getBaseRange();
            } else {
                return 0;
            }
        }
    }

    public static class RangeDownAugment extends RangeAugments {
        @Override
        public int getMaxAugments(ModuleItem moduleType) {
            if (moduleType instanceof IRangedModule) {
                IRangedModule r = (IRangedModule) moduleType;
                return r.getBaseRange() - 1;
            } else {
                return 0;
            }
        }
    }
}
