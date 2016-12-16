package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.recipe.enhancement.RedstoneEnhancementRecipe;
import mezz.jei.api.IJeiHelpers;

public class RedstoneEnhancementRecipeHandler extends BaseEnhancementRecipeHandler<RedstoneEnhancementRecipe> {
    public RedstoneEnhancementRecipeHandler(IJeiHelpers helpers) {
        super(helpers);
    }

    @Override
    public Class<RedstoneEnhancementRecipe> getRecipeClass() {
        return RedstoneEnhancementRecipe.class;
    }

}
