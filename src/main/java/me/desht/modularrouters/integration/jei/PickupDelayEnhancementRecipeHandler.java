package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.recipe.PickupDelayEnhancementRecipe;
import mezz.jei.api.IJeiHelpers;

public class PickupDelayEnhancementRecipeHandler extends BaseEnhancementRecipeHandler<PickupDelayEnhancementRecipe> {
    public PickupDelayEnhancementRecipeHandler(IJeiHelpers helpers) {
        super(helpers);
    }

    @Override
    public Class<PickupDelayEnhancementRecipe> getRecipeClass() {
        return PickupDelayEnhancementRecipe.class;
    }
}
