package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class PickupDelayAugment extends Augment {
    public static final int TICKS_PER_AUGMENT = 10;

    @Override
    public boolean isCompatible(ItemModule.ModuleType moduleType) {
        return moduleType == ItemModule.ModuleType.DROPPER || moduleType == ItemModule.ModuleType.FLINGER;
    }

    @Override
    public String getExtraInfo(int nAugments, ItemStack stack) {
        int pickupDelay = nAugments * TICKS_PER_AUGMENT;
        return " - " + I18n.format("itemText.augments.pickupDelay", pickupDelay, pickupDelay / 20.0f);
    }
}
