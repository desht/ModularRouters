package me.desht.modularrouters.recipe;

import me.desht.modularrouters.core.ModRecipes;
import me.desht.modularrouters.item.module.IPickaxeUser;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ResetModuleRecipe extends CustomRecipe {
    public ResetModuleRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level wrldIn) {
        ModuleItem module = null;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (module != null || !(stack.getItem() instanceof ModuleItem)) {
                    return false;
                }
                module = (ModuleItem) stack.getItem();
            }
        }
        return module != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack moduleStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ModuleItem) {
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
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MODULE_RESET.get();
    }
}
