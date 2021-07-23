package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface IPickaxeUser {
    String NBT_PICKAXE = "Pickaxe";

    default ItemStack getPickaxe(ItemStack moduleStack) {
        CompoundTag tag = moduleStack.getOrCreateTagElement(ModularRouters.MODID);
        if (tag.contains(NBT_PICKAXE)) {
            return ItemStack.of(tag.getCompound(NBT_PICKAXE));
        } else {
            return new ItemStack(Items.IRON_PICKAXE);
        }
    }

    default ItemStack setPickaxe(ItemStack moduleStack, ItemStack pickaxeStack) {
        CompoundTag tag = moduleStack.getOrCreateTagElement(ModularRouters.MODID);
        tag.put(NBT_PICKAXE, pickaxeStack.serializeNBT());
        return moduleStack;
    }
}
