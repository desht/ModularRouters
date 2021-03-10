package me.desht.modularrouters.recipe;

import me.desht.modularrouters.core.ModRecipes;
import me.desht.modularrouters.item.module.IPickaxeUser;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ResetModuleRecipe extends SpecialRecipe {
    public ResetModuleRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World wrldIn) {
        ItemModule module = null;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (module != null || !(stack.getItem() instanceof ItemModule)) {
                    return false;
                }
                module = (ItemModule) stack.getItem();
            }
        }
        return module != null;
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
        ItemStack moduleStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ItemModule) {
                moduleStack = stack;
                break;
            }
        }

        if (!moduleStack.isEmpty()) {
            ItemStack newStack = new ItemStack(moduleStack.getItem());
            ModuleHelper.validateNBT(newStack);
            if (moduleStack.getItem() instanceof IPickaxeUser) {
                ItemStack pick = ((IPickaxeUser) moduleStack.getItem()).getPickaxe(moduleStack);
                if (!pick.isEmpty()) ((IPickaxeUser) moduleStack.getItem()).setPickaxe(newStack, pick);
            }
            return newStack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.MODULE_RESET.get();
    }
}
