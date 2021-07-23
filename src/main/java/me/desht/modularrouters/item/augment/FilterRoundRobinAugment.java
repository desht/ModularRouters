package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.CreativeModule;
import me.desht.modularrouters.item.module.ModuleItem;

public class FilterRoundRobinAugment extends AugmentItem {
    @Override
    public int getMaxAugments(ModuleItem moduleType) {
        return moduleType instanceof CreativeModule ? 0 : 1;
    }
}
