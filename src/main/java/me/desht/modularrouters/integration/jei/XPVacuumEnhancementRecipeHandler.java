package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.recipe.enhancement.XPVacuumEnhancementRecipe;
import mezz.jei.api.IJeiHelpers;

public class XPVacuumEnhancementRecipeHandler extends BaseEnhancementRecipeHandler<XPVacuumEnhancementRecipe> {
    public XPVacuumEnhancementRecipeHandler(IJeiHelpers helpers) {
        super(helpers);
    }

    @Override
    public Class<XPVacuumEnhancementRecipe> getRecipeClass() {
        return XPVacuumEnhancementRecipe.class;
    }
}
