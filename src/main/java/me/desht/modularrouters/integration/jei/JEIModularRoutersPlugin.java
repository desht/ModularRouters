package me.desht.modularrouters.integration.jei;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class JEIModularRoutersPlugin extends BlankModPlugin {
    @Override
    public void register(IModRegistry registry) {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();

        registry.addRecipeHandlers(
                new RedstoneEnhancementRecipeHandler(jeiHelpers),
                new RegulatorEnhancementRecipeHandler(jeiHelpers),
                new PickupDelayEnhancementRecipeHandler(jeiHelpers)
        );
    }
}
