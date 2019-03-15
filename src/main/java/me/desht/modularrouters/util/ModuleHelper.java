package me.desht.modularrouters.util;

import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleFlags;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Collection of static convenience methods for managing NBT data in a module itemstack.
 */
public class ModuleHelper {
    public static final String NBT_FLAGS = "Flags";
    public static final String NBT_REDSTONE_MODE = "RedstoneMode";
    public static final String NBT_REGULATOR_AMOUNT = "RegulatorAmount";
    public static final String NBT_FILTER = "ModuleFilter";
    public static final String NBT_AUGMENTS = "Augments";

    @Nonnull
    public static NBTTagCompound validateNBT(ItemStack stack) {
        NBTTagCompound compound = stack.getOrCreateTag();
        if (compound.getTagId(NBT_FLAGS) != Constants.NBT.TAG_BYTE) {
            byte flags = 0x0;
            for (ModuleFlags b : ModuleFlags.values()) {
                if (b.getDefaultValue()) {
                    flags |= b.getMask();
                }
            }
            compound.putByte(NBT_FLAGS, flags);
        }
        if (compound.getTagId(NBT_FILTER) != Constants.NBT.TAG_COMPOUND) {
            compound.put(NBT_FILTER, new NBTTagCompound());
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

    public static boolean terminates(ItemStack stack) {
        return checkFlag(stack, ModuleFlags.TERMINATE);
    }

    public static boolean checkFlag(ItemStack stack, ModuleFlags flag) {
        NBTTagCompound compound = validateNBT(stack);
        return (compound.getByte(NBT_FLAGS) & flag.getMask()) != 0x0;
    }

    public static ItemModule.RelativeDirection getDirectionFromNBT(ItemStack stack) {

        if (stack.getItem() instanceof ItemModule && ((ItemModule) stack.getItem()).isDirectional()) {
            NBTTagCompound compound = validateNBT(stack);
            return ItemModule.RelativeDirection.values()[(compound.getByte(NBT_FLAGS) & 0x70) >> 4];
        } else {
            return ItemModule.RelativeDirection.NONE;
        }
    }

    public static int getRegulatorAmount(ItemStack itemstack) {
        NBTTagCompound compound = validateNBT(itemstack);
        return compound.getInt(NBT_REGULATOR_AMOUNT);
    }

    public static RouterRedstoneBehaviour getRedstoneBehaviour(ItemStack stack) {
        ItemAugment.AugmentCounter counter = new ItemAugment.AugmentCounter(stack);
        if (counter.getAugmentCount(ObjectRegistry.REDSTONE_AUGMENT) > 0) {
            NBTTagCompound compound = validateNBT(stack);
            try {
                return RouterRedstoneBehaviour.values()[compound.getByte(NBT_REDSTONE_MODE)];
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                return RouterRedstoneBehaviour.ALWAYS;
            }
        } else {
            return RouterRedstoneBehaviour.ALWAYS;
        }
    }

    public static NBTTagList getFilterItems(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return compound.getList(NBT_FILTER, Constants.NBT.TAG_COMPOUND);
    }

    public static int getRangeModifier(ItemStack stack) {
        ItemAugment.AugmentCounter counter = new ItemAugment.AugmentCounter(stack);
        return counter.getAugmentCount(ObjectRegistry.RANGE_UP_AUGMENT) - counter.getAugmentCount(ObjectRegistry.RANGE_DOWN_AUGMENT);
    }
}
