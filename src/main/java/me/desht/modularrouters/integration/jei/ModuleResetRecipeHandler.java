package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.recipe.ModuleResetRecipe;
import mezz.jei.api.IJeiHelpers;

public class ModuleResetRecipeHandler extends BaseEnhancementRecipeHandler<ModuleResetRecipe> {
    ModuleResetRecipeHandler(IJeiHelpers helpers) {
        super(helpers);
    }

    @Override
    public Class<ModuleResetRecipe> getRecipeClass() {
        return ModuleResetRecipe.class;
    }
}
