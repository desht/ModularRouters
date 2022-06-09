package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.DropperModule;
import me.desht.modularrouters.item.module.ModuleItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class PickupDelayAugment extends AugmentItem {
    public static final int TICKS_PER_AUGMENT = 10;

    @Override
    public int getMaxAugments(ModuleItem moduleType) {
        return moduleType instanceof DropperModule ? 20 : 0;  // includes flinger module
    }

    @Override
    public Component getExtraInfo(int nAugments, ItemStack moduleStack) {
        int pickupDelay = nAugments * TICKS_PER_AUGMENT;
        return Component.literal(" - ").append(xlate("modularrouters.itemText.augments.pickupDelay", pickupDelay, pickupDelay / 20.0f));
    }
}
