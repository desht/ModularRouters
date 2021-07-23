package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ExtruderModule1;
import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.item.module.ModuleItem;

public class PushingAugment extends AugmentItem {
    @Override
    public int getMaxAugments(ModuleItem moduleType) {
        return moduleType instanceof ExtruderModule1 || moduleType instanceof ExtruderModule2 ? 64 : 0;
    }
}
