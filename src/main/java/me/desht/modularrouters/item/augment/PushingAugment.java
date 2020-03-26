package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ExtruderModule1;
import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.item.module.ItemModule;

public class PushingAugment extends ItemAugment {
    @Override
    public int getMaxAugments(ItemModule moduleType) {
        return moduleType instanceof ExtruderModule1 || moduleType instanceof ExtruderModule2 ? 64 : 0;
    }
}
