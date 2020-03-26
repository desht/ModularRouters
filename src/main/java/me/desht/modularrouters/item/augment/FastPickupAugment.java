package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.VacuumModule;

public class FastPickupAugment extends ItemAugment {
    @Override
    public int getMaxAugments(ItemModule moduleType) {
        return moduleType instanceof VacuumModule ? 1 : 0;
    }
}
