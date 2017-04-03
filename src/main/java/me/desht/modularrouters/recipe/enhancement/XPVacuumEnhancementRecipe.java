package me.desht.modularrouters.recipe.enhancement;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;

public class XPVacuumEnhancementRecipe extends ModuleEnhancementRecipe {
    XPVacuumEnhancementRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    protected boolean validateItem(ItemStack stack) {
        return ItemModule.isType(stack, ItemModule.ModuleType.VACUUM) && !ModuleHelper.hasXPVacuum(stack);
    }

    @Override
    public void enableUpgrade(ItemStack stack) {
        ModuleHelper.enableXPVacuum(stack);
    }

    @Override
    public String getRecipeId() {
        return "xpVacuum";
    }
}
