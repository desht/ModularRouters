package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;

public class PickupDelayEnhancementRecipe extends ModuleEnhancementRecipe {
    PickupDelayEnhancementRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    protected boolean validateItem(ItemStack stack) {
        return ItemModule.isType(stack, ItemModule.ModuleType.FLINGER) || ItemModule.isType(stack, ItemModule.ModuleType.DROPPER);
    }

    @Override
    public void enableUpgrade(ItemStack stack) {
        ModuleHelper.increasePickupDelay(stack);
    }

    @Override
    public String getRecipeId() {
        return "pickupDelay";
    }
}
