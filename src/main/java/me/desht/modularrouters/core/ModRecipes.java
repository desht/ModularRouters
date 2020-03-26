package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.recipe.EnchantModuleRecipe;
import me.desht.modularrouters.recipe.GuideBookRecipe;
import me.desht.modularrouters.recipe.ResetModuleRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipes {
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, ModularRouters.MODID);

    public static final RegistryObject<SpecialRecipeSerializer<EnchantModuleRecipe>> MODULE_ENCHANT
            = RECIPES.register("module_enchant", () -> new SpecialRecipeSerializer<>(EnchantModuleRecipe::new));
    public static final RegistryObject<SpecialRecipeSerializer<ResetModuleRecipe>> MODULE_RESET
            = RECIPES.register("module_reset", () -> new SpecialRecipeSerializer<>(ResetModuleRecipe::new));
    public static final RegistryObject<SpecialRecipeSerializer<GuideBookRecipe>> GUIDE_BOOK
            = RECIPES.register("guide_book", () -> new SpecialRecipeSerializer<>(GuideBookRecipe::new));
}
