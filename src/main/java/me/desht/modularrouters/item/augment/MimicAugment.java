package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.item.module.ModuleItem;

public class MimicAugment extends AugmentItem {
    @Override
    public int getMaxAugments(ModuleItem moduleType) {
        return moduleType instanceof ExtruderModule2 ? 1 : 0;
    }
}
