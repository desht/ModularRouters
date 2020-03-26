package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ItemModule;

public abstract class RangeAugments extends ItemAugment {
    public static class RangeUpAugment extends RangeAugments {
        @Override
        public int getMaxAugments(ItemModule moduleType) {
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
        public int getMaxAugments(ItemModule moduleType) {
            if (moduleType instanceof IRangedModule) {
                IRangedModule r = (IRangedModule) moduleType;
                return r.getBaseRange() - 1;
            } else {
                return 0;
            }
        }
    }
}
