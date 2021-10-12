package me.desht.modularrouters.util;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.module.ModuleItem.ModuleFlags;
import me.desht.modularrouters.item.module.ModuleItem.RelativeDirection;
import me.desht.modularrouters.item.module.ModuleItem.Termination;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Collection of static convenience methods for managing NBT data in a module itemstack.
 */
public class ModuleHelper {
    public static final String NBT_DIRECTION = "Direction";
    public static final String NBT_REDSTONE_MODE = "RedstoneMode";
    public static final String NBT_REGULATOR_AMOUNT = "RegulatorAmount";
    public static final String NBT_FILTER = "ModuleFilter";
    public static final String NBT_AUGMENTS = "Augments";
    public static final String NBT_MATCH_ALL = "MatchAll";
    public static final String NBT_TERMINATION = "Termination";
    private static final String NBT_RR_COUNTER = "RoundRobinCounter";

    @Nonnull
    public static CompoundTag validateNBT(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTagElement(ModularRouters.MODID);
        if (compound.getTagType(NBT_FILTER) != Tag.TAG_COMPOUND) {
            compound.put(NBT_FILTER, new CompoundTag());
        }
        return compound;
    }

    public static boolean isBlacklist(ItemStack stack) {
        return checkFlag(stack, ModuleFlags.BLACKLIST);
    }

    public static boolean ignoreDamage(ItemStack stack) {
        return checkFlag(stack, ModuleFlags.IGNORE_DAMAGE);
    }

    public static boolean ignoreNBT(ItemStack stack) {
        return checkFlag(stack, ModuleFlags.IGNORE_NBT);
    }

    public static boolean ignoreTags(ItemStack stack) {
        return checkFlag(stack, ModuleFlags.IGNORE_TAGS);
    }

    public static boolean checkFlag(ItemStack stack, ModuleFlags flag) {
        CompoundTag tag = validateNBT(stack);
        return tag.contains(flag.getName(), Tag.TAG_BYTE) ? tag.getBoolean(flag.getName()) : flag.getDefaultValue();
    }

    public static Termination getTermination(ItemStack stack) {
        CompoundTag compound = validateNBT(stack);
        try {
            return compound.contains(ModuleHelper.NBT_TERMINATION, Tag.TAG_STRING) ?
                    Termination.valueOf(compound.getString(ModuleHelper.NBT_TERMINATION)) :
                    Termination.NONE;
        } catch (IllegalArgumentException e) {
            compound.putString(ModuleHelper.NBT_TERMINATION, Termination.NONE.toString());
            return Termination.NONE;
        }
    }

    public static RelativeDirection getRelativeDirection(ItemStack stack) {
        if (stack.getItem() instanceof ModuleItem && ((ModuleItem) stack.getItem()).isDirectional()) {
            CompoundTag compound = validateNBT(stack);
            try {
                return RelativeDirection.valueOf(compound.getString(NBT_DIRECTION));
            } catch (IllegalArgumentException e) {
                compound.putString(NBT_DIRECTION, RelativeDirection.NONE.toString());
                return RelativeDirection.NONE;
            }
        } else {
            return RelativeDirection.NONE;
        }
    }

    public static int getRegulatorAmount(ItemStack itemstack) {
        return validateNBT(itemstack).getInt(NBT_REGULATOR_AMOUNT);
    }

    public static RouterRedstoneBehaviour getRedstoneBehaviour(ItemStack stack) {
        AugmentItem.AugmentCounter counter = new AugmentItem.AugmentCounter(stack);
        if (counter.getAugmentCount(ModItems.REDSTONE_AUGMENT.get()) > 0) {
            CompoundTag compound = validateNBT(stack);
            try {
                return RouterRedstoneBehaviour.values()[compound.getByte(NBT_REDSTONE_MODE)];
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                return RouterRedstoneBehaviour.ALWAYS;
            }
        } else {
            return RouterRedstoneBehaviour.ALWAYS;
        }
    }

    public static int getRangeModifier(ItemStack stack) {
        AugmentItem.AugmentCounter counter = new AugmentItem.AugmentCounter(stack);
        return counter.getAugmentCount(ModItems.RANGE_UP_AUGMENT.get()) - counter.getAugmentCount(ModItems.RANGE_DOWN_AUGMENT.get());
    }

    public static boolean isMatchAll(ItemStack stack) {
        return validateNBT(stack).getBoolean(NBT_MATCH_ALL);
    }

    public static void setRoundRobinCounter(ItemStack moduleStack, int counter) {
        CompoundTag tag = validateNBT(moduleStack);
        tag.putInt(NBT_RR_COUNTER, counter);
    }

    public static int getRoundRobinCounter(ItemStack moduleStack) {
        return validateNBT(moduleStack).getInt(NBT_RR_COUNTER);
    }
}
