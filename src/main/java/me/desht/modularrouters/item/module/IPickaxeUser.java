package me.desht.modularrouters.item.module;

import me.desht.modularrouters.core.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;

public interface IPickaxeUser {
    ItemContainerContents DEFAULT_PICK = ItemContainerContents.fromItems(List.of(new ItemStack(Items.STONE_PICKAXE)));

    static ItemStack getPickaxe(ItemStack moduleStack) {
        return moduleStack.getItem() instanceof IPickaxeUser ?
                moduleStack.getOrDefault(ModDataComponents.PICKAXE, DEFAULT_PICK).copyOne() :
                ItemStack.EMPTY;
    }

    static ItemStack setPickaxe(ItemStack moduleStack, ItemStack pickaxeStack) {
        if (moduleStack.getItem() instanceof IPickaxeUser) {
            pickaxeStack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            moduleStack.set(ModDataComponents.PICKAXE, ItemContainerContents.fromItems(List.of(pickaxeStack)));
        }
        return moduleStack;
    }
}
