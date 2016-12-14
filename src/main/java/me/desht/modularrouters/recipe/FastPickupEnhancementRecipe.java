package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;

public class FastPickupEnhancementRecipe extends ModuleEnhancementRecipe {
    FastPickupEnhancementRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    protected boolean validateItem(ItemStack stack) {
        return ItemModule.isType(stack, ItemModule.ModuleType.VACUUM) && !ModuleHelper.hasFastPickup(stack);
    }

    @Override
    public void enableUpgrade(ItemStack stack) {
        ModuleHelper.addFastPickup(stack);
    }

    @Override
    public String getRecipeId() {
        return "fastPickup";
    }
}
