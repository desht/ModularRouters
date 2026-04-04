package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.recipe.GuideBookRecipe;
import me.desht.modularrouters.recipe.PickaxeModuleRecipe.BreakerModuleRecipe;
import me.desht.modularrouters.recipe.PickaxeModuleRecipe.ExtruderModule1Recipe;
import me.desht.modularrouters.recipe.ResetModuleRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ModularRouters.MODID);

    public static final Supplier<RecipeSerializer<BreakerModuleRecipe>> BREAKER_MODULE
            = RECIPES.register("breaker_module", () -> BreakerModuleRecipe.SERIALIZER);
    public static final Supplier<RecipeSerializer<ExtruderModule1Recipe>> EXTRUDER_MODULE_1
            = RECIPES.register("extruder_module_1", () -> ExtruderModule1Recipe.SERIALIZER);

    public static final Supplier<RecipeSerializer<ResetModuleRecipe>> MODULE_RESET
            = RECIPES.register("module_reset", () -> ResetModuleRecipe.SERIALIZER);
    public static final Supplier<RecipeSerializer<GuideBookRecipe>> GUIDE_BOOK
            = RECIPES.register("guide_book", () -> GuideBookRecipe.SERIALIZER);
}
