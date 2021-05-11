package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.CreativeModule;
import me.desht.modularrouters.item.module.ItemModule;

public class FilterRoundRobinAugment extends ItemAugment {
    @Override
    public int getMaxAugments(ItemModule moduleType) {
        return moduleType instanceof CreativeModule ? 0 : 1;
    }
}
