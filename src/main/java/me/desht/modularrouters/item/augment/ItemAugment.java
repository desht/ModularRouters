package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.item.ItemSubTypes;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemAugment extends ItemSubTypes<ItemAugment.AugmentType> {

    public enum AugmentType {
        FAST_PICKUP,
        PICKUP_DELAY,
        RANGE_UP,
        RANGE_DOWN,
        REDSTONE,
        REGULATOR,
        STACK,
        XP_VACUUM,
        MIMIC,
        PUSHING;

        public static AugmentType getType(ItemStack stack) {
            return stack.getItem() instanceof ItemAugment ? values()[stack.getItemDamage()] : null;
        }
    }

    public ItemAugment() {
        super("augment", AugmentType.class);

        register(AugmentType.FAST_PICKUP, new FastPickupAugment());
        register(AugmentType.PICKUP_DELAY, new PickupDelayAugment());
        register(AugmentType.RANGE_UP, new RangeAugments.RangeUpAugment());
        register(AugmentType.RANGE_DOWN, new RangeAugments.RangeDownAugment());
        register(AugmentType.REDSTONE, new RedstoneAugment());
        register(AugmentType.REGULATOR, new RegulatorAugment());
        register(AugmentType.STACK, new StackAugment());
        register(AugmentType.XP_VACUUM, new XPVacuumAugment());
        register(AugmentType.MIMIC, new MimicAugment());
        register(AugmentType.PUSHING, new PushingAugment());
    }

    public static Augment getAugment(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemAugment) || stack.getMetadata() >= AugmentType.values().length) {
            return null;
        }
        return getAugment(AugmentType.values()[stack.getMetadata()]);
    }

    public static ItemStack makeItemStack(AugmentType type) {
        return makeItemStack(type, 1);
    }

    public static ItemStack makeItemStack(AugmentType type, int amount) {
        return new ItemStack(RegistrarMR.AUGMENT, amount, type.ordinal());
    }

    @Nonnull
    public static Augment getAugment(AugmentType type) {
        return (Augment) RegistrarMR.AUGMENT.getHandler(type);
    }

    public static class AugmentCounter {
        private int[] counts = new int[AugmentType.values().length];

        public AugmentCounter(ItemStack moduleStack) {
            if (!(moduleStack.getItem() instanceof ItemModule)) {
                throw new IllegalArgumentException("item is not a ItemModule: " + moduleStack);
            }

            AugmentHandler h = new AugmentHandler(moduleStack);
            for (int i = 0; i < h.getSlots(); i++) {
                ItemStack augmentStack = h.getStackInSlot(i);
                AugmentType type = AugmentType.getType(augmentStack);
                if (type != null) {
                    counts[type.ordinal()] += augmentStack.getCount();
                }
            }
        }

        public int getAugmentCount(AugmentType type) {
            return counts[type.ordinal()];
        }
    }
}
