package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.recipe.RegulatorEnhancementRecipe;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class RegulatorEnhancementRecipeHandler implements IRecipeHandler<RegulatorEnhancementRecipe> {
    @Override
    public Class<RegulatorEnhancementRecipe> getRecipeClass() {
        return RegulatorEnhancementRecipe.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        return ModularRouters.modId + ".moduleEnhancement";
    }

    @Override
    public String getRecipeCategoryUid(RegulatorEnhancementRecipe recipe) {
        return ModularRouters.modId + ".moduleEnhancement";
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RegulatorEnhancementRecipe recipe) {
        return new ModuleEnhancementRecipeWrapper(recipe);
    }

    @Override
    public boolean isRecipeValid(RegulatorEnhancementRecipe recipe) {
        return recipe.getRecipeOutput() != null;
    }
}
