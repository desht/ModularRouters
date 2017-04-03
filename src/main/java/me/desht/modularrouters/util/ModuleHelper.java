package me.desht.modularrouters.util;

import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

/**
 * Collection of static convenience methods for managing NBT data in a module itemstack.
 */
public class ModuleHelper {
    public static final String NBT_FLAGS = "Flags";
    public static final String NBT_REDSTONE_ENABLED = "RedstoneEnabled";
    public static final String NBT_REDSTONE_MODE = "RedstoneMode";
    public static final String NBT_REGULATOR_ENABLED = "RegulatorEnabled";
    public static final String NBT_REGULATOR_AMOUNT = "RegulatorAmount";
    public static final String NBT_FILTER = "ModuleFilter";
    public static final String NBT_PICKUP_DELAY = "PickupDelay";
    private static final String NBT_FAST_PICKUP = "FastPickup";
    private static final String NBT_XP_VACUUM = "XPVacuum";

    @Nonnull
    public static NBTTagCompound validateNBT(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            stack.setTagCompound(compound = new NBTTagCompound());
        }
        if (compound.getTagId(NBT_FLAGS) != Constants.NBT.TAG_BYTE) {
            byte flags = 0x0;
            for (Module.ModuleFlags b : Module.ModuleFlags.values()) {
                if (b.getDefaultValue()) {
                    flags |= b.getMask();
                }
            }
            compound.setByte(NBT_FLAGS, flags);
        }
        if (compound.getTagId(NBT_FILTER) != Constants.NBT.TAG_LIST) {
            compound.setTag(NBT_FILTER, new NBTTagList());
        }
        return compound;
    }

    public static boolean isBlacklist(ItemStack stack) {
        return checkFlag(stack, Module.ModuleFlags.BLACKLIST);
    }

    public static boolean ignoreMeta(ItemStack stack) {
        return checkFlag(stack, Module.ModuleFlags.IGNORE_META);
    }

    public static boolean ignoreNBT(ItemStack stack) {
        return checkFlag(stack, Module.ModuleFlags.IGNORE_NBT);
    }

    public static boolean ignoreOreDict(ItemStack stack) {
        return checkFlag(stack, Module.ModuleFlags.IGNORE_OREDICT);
    }

    public static boolean terminates(ItemStack stack) {
        return checkFlag(stack, Module.ModuleFlags.TERMINATE);
    }

    public static boolean checkFlag(ItemStack stack, Module.ModuleFlags flag) {
        NBTTagCompound compound = validateNBT(stack);
        return (compound.getByte(NBT_FLAGS) & flag.getMask()) != 0x0;
    }

    public static Module.RelativeDirection getDirectionFromNBT(ItemStack stack) {
        Module module = ItemModule.getModule(stack);
        if (module == null || !module.isDirectional()) {
            return Module.RelativeDirection.NONE;
        }
        NBTTagCompound compound = validateNBT(stack);
        return Module.RelativeDirection.values()[(compound.getByte(NBT_FLAGS) & 0x70) >> 4];
    }

    public static boolean isRedstoneBehaviourEnabled(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return compound.getBoolean(NBT_REDSTONE_ENABLED);
    }

    public static boolean isRegulatorEnabled(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return compound.getBoolean(NBT_REGULATOR_ENABLED);
    }

    public static int getRegulatorAmount(ItemStack itemstack) {
        NBTTagCompound compound = validateNBT(itemstack);
        return compound.getInteger(NBT_REGULATOR_AMOUNT);
    }

    public static RouterRedstoneBehaviour getRedstoneBehaviour(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        if (compound.getBoolean(NBT_REDSTONE_ENABLED)) {
            try {
                // check for mode stored as a string (v1.0), or as a byte (v1.1+)
                int id = compound.getTagId(NBT_REDSTONE_MODE);
                if (id == Constants.NBT.TAG_BYTE) {
                    return RouterRedstoneBehaviour.values()[compound.getByte(NBT_REDSTONE_MODE)];
                } else if (id == Constants.NBT.TAG_STRING) {
                    return RouterRedstoneBehaviour.valueOf(compound.getString(NBT_REDSTONE_MODE));
                } else {
                    return RouterRedstoneBehaviour.ALWAYS;
                }
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                return RouterRedstoneBehaviour.ALWAYS;
            }
        } else {
            return RouterRedstoneBehaviour.ALWAYS;
        }
    }

    public static void setRedstoneBehaviour(ItemStack stack, boolean enabled, RouterRedstoneBehaviour behaviour) {
        NBTTagCompound compound = validateNBT(stack);
        compound.setBoolean(NBT_REDSTONE_ENABLED, enabled);
        compound.setByte(NBT_REDSTONE_MODE, (byte) behaviour.ordinal());
    }

    public static void setRegulatorAmount(ItemStack stack, boolean enabled, int amount) {
        NBTTagCompound compound = validateNBT(stack);
        compound.setBoolean(NBT_REGULATOR_ENABLED, enabled);
        compound.setInteger(NBT_REGULATOR_AMOUNT, amount);
    }

    public static NBTTagList getFilterItems(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return compound.getTagList(NBT_FILTER, Constants.NBT.TAG_COMPOUND);
    }

    public static void increasePickupDelay(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        int delay = compound.getInteger(NBT_PICKUP_DELAY);
        compound.setInteger(NBT_PICKUP_DELAY, delay + 10);  // ticks
    }

    public static int getPickupDelay(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return compound.getInteger(NBT_PICKUP_DELAY);
    }

    public static void addFastPickup(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        compound.setBoolean(NBT_FAST_PICKUP, true);
    }

    public static boolean hasFastPickup(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return compound.getBoolean(NBT_FAST_PICKUP);
    }

    public static ItemStack makeItemStack(ItemModule.ModuleType type) {
        return makeItemStack(type, 1);
    }

    public static ItemStack makeItemStack(ItemModule.ModuleType type, int amount) {
        return new ItemStack(ModItems.module, amount, type.ordinal());
    }

    public static void enableXPVacuum(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        compound.setBoolean(NBT_XP_VACUUM, true);
    }

    public static boolean hasXPVacuum(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return compound.getBoolean(NBT_XP_VACUUM);
    }
}
