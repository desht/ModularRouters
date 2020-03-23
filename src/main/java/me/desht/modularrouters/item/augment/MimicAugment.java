package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.item.module.ItemModule;

public class MimicAugment extends ItemAugment {
    @Override
    public boolean isCompatible(ItemModule moduleType) {
        return moduleType instanceof ExtruderModule2;
    }
}
