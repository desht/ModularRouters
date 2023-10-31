package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.recipe.GuideBookRecipe;
import me.desht.modularrouters.recipe.PickaxeModuleRecipe.BreakerModuleRecipe;
import me.desht.modularrouters.recipe.PickaxeModuleRecipe.ExtruderModule1Recipe;
import me.desht.modularrouters.recipe.ResetModuleRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ModularRouters.MODID);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<BreakerModuleRecipe>> BREAKER_MODULE
            = RECIPES.register("breaker_module", () -> new SimpleCraftingRecipeSerializer<>(BreakerModuleRecipe::new));
    public static final RegistryObject<SimpleCraftingRecipeSerializer<ExtruderModule1Recipe>> EXTRUDER_MODULE_1
            = RECIPES.register("extruder_module_1", () -> new SimpleCraftingRecipeSerializer<>(ExtruderModule1Recipe::new));

    public static final RegistryObject<SimpleCraftingRecipeSerializer<ResetModuleRecipe>> MODULE_RESET
            = RECIPES.register("module_reset", () -> new SimpleCraftingRecipeSerializer<>(ResetModuleRecipe::new));
    public static final RegistryObject<SimpleCraftingRecipeSerializer<GuideBookRecipe>> GUIDE_BOOK
            = RECIPES.register("guide_book", () -> new SimpleCraftingRecipeSerializer<>(GuideBookRecipe::new));
}
