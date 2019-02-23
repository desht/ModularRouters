package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.VacuumModule;

public class XPVacuumAugment extends ItemAugment {
    public XPVacuumAugment(Properties props) {
        super(props);
    }

    @Override
    public boolean isCompatible(ItemModule moduleType) {
        return moduleType instanceof VacuumModule;
    }
}
