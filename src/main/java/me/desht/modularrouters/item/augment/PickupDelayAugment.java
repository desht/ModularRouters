package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.DropperModule;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

public class PickupDelayAugment extends ItemAugment {
    public static final int TICKS_PER_AUGMENT = 10;

    @Override
    public int getMaxAugments(ItemModule moduleType) {
        return moduleType instanceof DropperModule ? 20 : 0;  // includes flinger module
    }

    @Override
    public String getExtraInfo(int nAugments, ItemStack moduleStack) {
        int pickupDelay = nAugments * TICKS_PER_AUGMENT;
        return " - " + I18n.get("modularrouters.itemText.augments.pickupDelay", pickupDelay, pickupDelay / 20.0f);
    }
}
