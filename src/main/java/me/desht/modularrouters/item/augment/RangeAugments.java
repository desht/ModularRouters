package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ItemModule;

public abstract class RangeAugments extends ItemAugment {
    public RangeAugments(Properties props) {
        super(props);
    }

    @Override
    public boolean isCompatible(ItemModule moduleType) {
        return moduleType instanceof IRangedModule;
    }

    public static class RangeUpAugment extends RangeAugments {
        public RangeUpAugment(Properties props) {
            super(props);
        }
    }

    public static class RangeDownAugment extends RangeAugments {
        public RangeDownAugment(Properties props) {
            super(props);
        }
    }
}
