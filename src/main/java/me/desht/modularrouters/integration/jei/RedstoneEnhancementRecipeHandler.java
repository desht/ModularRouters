package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.recipe.RedstoneEnhancementRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class RedstoneEnhancementRecipeHandler implements IRecipeHandler<RedstoneEnhancementRecipe> {
    @Override
    public Class<RedstoneEnhancementRecipe> getRecipeClass() {
        return RedstoneEnhancementRecipe.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        return ModularRouters.modId + ".moduleEnhancement";
    }

    @Override
    public String getRecipeCategoryUid(RedstoneEnhancementRecipe recipe) {
        return ModularRouters.modId + ".moduleEnhancement";
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RedstoneEnhancementRecipe recipe) {
        return new ModuleEnhancementRecipeWrapper(recipe);
    }

    @Override
    public boolean isRecipeValid(RedstoneEnhancementRecipe recipe) {
        return recipe.getRecipeOutput() != null;
    }
}
