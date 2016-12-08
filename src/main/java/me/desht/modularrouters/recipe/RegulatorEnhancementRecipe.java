package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;

public class RegulatorEnhancementRecipe extends ModuleEnhancementRecipe {
    RegulatorEnhancementRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    public void enableUpgrade(ItemStack stack) {
        ModuleHelper.setRegulatorAmount(stack, true, 1);
    }

    @Override
    public String getRecipeId() {
        return "regulator";
    }

    static boolean appliesTo(ItemModule.ModuleType type) {
        return ItemModule.getModule(type).canBeRegulated();
    }

    @Override
    protected boolean validateItem(ItemStack stack) {
        return !ModuleHelper.isRegulatorEnabled(stack);
    }
}
