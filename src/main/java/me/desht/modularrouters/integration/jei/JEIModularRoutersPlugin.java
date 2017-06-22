package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.recipe.enhancement.*;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

@JEIPlugin
public class JEIModularRoutersPlugin implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();

        registry.handleRecipes(RedstoneEnhancementRecipe.class, recipe -> new ModuleEnhancementRecipeWrapper(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(RegulatorEnhancementRecipe.class, recipe -> new ModuleEnhancementRecipeWrapper(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(PickupDelayEnhancementRecipe.class, recipe -> new ModuleEnhancementRecipeWrapper(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(FastPickupEnhancementRecipe.class, recipe -> new ModuleEnhancementRecipeWrapper(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(ModuleResetRecipe.class, recipe -> new ModuleEnhancementRecipeWrapper(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(XPVacuumEnhancementRecipe.class, recipe -> new ModuleEnhancementRecipeWrapper(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(RangeUpRecipe.class, recipe -> new ModuleEnhancementRecipeWrapper(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(RangeDownRecipe.class, recipe -> new ModuleEnhancementRecipeWrapper(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);
    }
}
