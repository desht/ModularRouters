package me.desht.modularrouters.recipe;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.RecipeSerializers;

public class ModRecipes {
    static final IRecipeSerializer<EnchantModuleRecipe> ENCHANT_MODULE = RecipeSerializers.register(
            new RecipeSerializers.SimpleSerializer<>("modularrouters:module_enchant", EnchantModuleRecipe::new)
    );
    static final IRecipeSerializer<ResetModuleRecipe> RESET_MODULE = RecipeSerializers.register(
            new RecipeSerializers.SimpleSerializer<>("modularrouters:module_reset", ResetModuleRecipe::new)
    );

    public static void init() {
    }
}
