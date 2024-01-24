package me.desht.modularrouters.item.module;

import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface IPickaxeUser {
    String NBT_PICKAXE = "Pickaxe";

    default ItemStack getPickaxe(ItemStack moduleStack) {
        CompoundTag tag = ModuleHelper.validateNBT(moduleStack);
        if (tag.contains(NBT_PICKAXE)) {
            return ItemStack.of(tag.getCompound(NBT_PICKAXE));
        } else {
            return new ItemStack(Items.IRON_PICKAXE);
        }
    }

    default ItemStack setPickaxe(ItemStack moduleStack, ItemStack pickaxeStack) {
        ModuleHelper.validateNBTForWriting(moduleStack).put(NBT_PICKAXE, pickaxeStack.serializeNBT());
        return moduleStack;
    }
}
