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
        registry.handleRecipes(ModuleResetRecipe.class, recipe -> new ModuleEnhancementRecipeWrapper(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);
    }
}
