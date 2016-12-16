package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.recipe.enhancement.FastPickupEnhancementRecipe;
import mezz.jei.api.IJeiHelpers;

public class FastPickupEnhancementRecipeHandler extends BaseEnhancementRecipeHandler<FastPickupEnhancementRecipe> {
    public FastPickupEnhancementRecipeHandler(IJeiHelpers helpers) {
        super(helpers);
    }

    @Override
    public Class<FastPickupEnhancementRecipe> getRecipeClass() {
        return FastPickupEnhancementRecipe.class;
    }
}
