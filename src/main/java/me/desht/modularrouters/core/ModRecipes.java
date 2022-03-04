package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.recipe.GuideBookRecipe;
import me.desht.modularrouters.recipe.PickaxeModuleRecipe.BreakerModuleRecipe;
import me.desht.modularrouters.recipe.PickaxeModuleRecipe.ExtruderModule1Recipe;
import me.desht.modularrouters.recipe.ResetModuleRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ModularRouters.MODID);

    public static final RegistryObject<SimpleRecipeSerializer<BreakerModuleRecipe>> BREAKER_MODULE
            = RECIPES.register("breaker_module", () -> new SimpleRecipeSerializer<>(BreakerModuleRecipe::new));
    public static final RegistryObject<SimpleRecipeSerializer<ExtruderModule1Recipe>> EXTRUDER_MODULE_1
            = RECIPES.register("extruder_module_1", () -> new SimpleRecipeSerializer<>(ExtruderModule1Recipe::new));

    public static final RegistryObject<SimpleRecipeSerializer<ResetModuleRecipe>> MODULE_RESET
            = RECIPES.register("module_reset", () -> new SimpleRecipeSerializer<>(ResetModuleRecipe::new));
    public static final RegistryObject<SimpleRecipeSerializer<GuideBookRecipe>> GUIDE_BOOK
            = RECIPES.register("guide_book", () -> new SimpleRecipeSerializer<>(GuideBookRecipe::new));
}
