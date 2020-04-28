package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;

public interface IPickaxeUser {
    String NBT_PICKAXE = "Pickaxe";

    default ItemStack getPickaxe(ItemStack moduleStack) {
        CompoundNBT tag = moduleStack.getOrCreateChildTag(ModularRouters.MODID);
        if (tag.contains(NBT_PICKAXE)) {
            return ItemStack.read(tag.getCompound(NBT_PICKAXE));
        } else {
            return new ItemStack(Items.IRON_PICKAXE);
        }
    }

    default ItemStack setPickaxe(ItemStack moduleStack, ItemStack pickaxeStack) {
        CompoundNBT tag = moduleStack.getOrCreateChildTag(ModularRouters.MODID);
        tag.put(NBT_PICKAXE, pickaxeStack.serializeNBT());
        return moduleStack;
    }
}
