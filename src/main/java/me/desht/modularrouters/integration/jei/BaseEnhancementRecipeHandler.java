package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.recipe.ModuleEnhancementRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;

public abstract class BaseEnhancementRecipeHandler<T extends ModuleEnhancementRecipe> implements IRecipeHandler<T> {
    private final IJeiHelpers helpers;

    BaseEnhancementRecipeHandler(IJeiHelpers helpers) {
        this.helpers = helpers;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid() {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Override
    public String getRecipeCategoryUid(T recipe) {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(T recipe) {
        return new ModuleEnhancementRecipeWrapper(helpers, recipe);
    }

    @Override
    public boolean isRecipeValid(T recipe) {
        return recipe.getRecipeOutput() != null;
    }
}
