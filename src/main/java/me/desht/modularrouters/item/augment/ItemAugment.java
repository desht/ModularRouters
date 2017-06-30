package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.item.ItemSubTypes;
import net.minecraft.item.ItemStack;

public class ItemAugment extends ItemSubTypes<ItemAugment.AugmentType> {

    public enum AugmentType {
        FAST_PICKUP,
        PICKUP_DELAY,
        RANGE_UP,
        RANGE_DOWN,
        REDSTONE,
        REGULATOR,
        STACK,
        XP_VACUUM
    }

    public ItemAugment() {
        super("augment", AugmentType.class);

        register(AugmentType.FAST_PICKUP, new FastPickupAugment());
        register(AugmentType.PICKUP_DELAY, new PickupDelayAugment());
        register(AugmentType.RANGE_UP, new RangeUpAugment());
        register(AugmentType.RANGE_DOWN, new RangeDownAugment());
        register(AugmentType.REDSTONE, new RedstoneAugment());
        register(AugmentType.REGULATOR, new RegulatorAugment());
        register(AugmentType.STACK, new StackAugment());
        register(AugmentType.XP_VACUUM, new XPVacuumAugment());
    }

    public static Augment getAugment(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemAugment) || stack.getMetadata() >= AugmentType.values().length) {
            return null;
        }
        return getAugment(AugmentType.values()[stack.getMetadata()]);
    }

    public static Augment getAugment(AugmentType type) {
        return (Augment) RegistrarMR.AUGMENT.getHandler(type);
    }
}
