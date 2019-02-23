package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.VacuumModule;

public class FastPickupAugment extends ItemAugment {
    public FastPickupAugment(Properties props) {
        super(props);
    }

    @Override
    public boolean isCompatible(ItemModule moduleType) {
        return moduleType instanceof VacuumModule;
    }
}
