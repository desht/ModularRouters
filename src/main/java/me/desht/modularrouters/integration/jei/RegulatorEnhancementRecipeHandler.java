package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.recipe.enhancement.RegulatorEnhancementRecipe;
import mezz.jei.api.IJeiHelpers;

public class RegulatorEnhancementRecipeHandler extends BaseEnhancementRecipeHandler<RegulatorEnhancementRecipe> {
    public RegulatorEnhancementRecipeHandler(IJeiHelpers helpers) {
        super(helpers);
    }

    @Override
    public Class<RegulatorEnhancementRecipe> getRecipeClass() {
        return RegulatorEnhancementRecipe.class;
    }
}
