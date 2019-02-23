package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.item.module.ItemModule;

public class MimicAugment extends ItemAugment {
    public MimicAugment(Properties props) {
        super(props);
    }

    @Override
    public boolean isCompatible(ItemModule moduleType) {
        return moduleType instanceof ExtruderModule2;
    }
}
