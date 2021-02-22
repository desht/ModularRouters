package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.*;

public class FilterRoundRobinAugment extends ItemAugment {
    @Override
    public int getMaxAugments(ItemModule moduleType) {
        return moduleType instanceof CreativeModule ? 0 : 1;
    }
}
