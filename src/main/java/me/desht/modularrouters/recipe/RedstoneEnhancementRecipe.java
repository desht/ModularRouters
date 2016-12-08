package me.desht.modularrouters.recipe;

import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;

public class RedstoneEnhancementRecipe extends ModuleEnhancementRecipe {
    RedstoneEnhancementRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    public void enableUpgrade(ItemStack stack) {
        ModuleHelper.setRedstoneBehaviour(stack, true, RouterRedstoneBehaviour.ALWAYS);
    }

    @Override
    public String getRecipeId() {
        return "redstone";
    }

    @Override
    protected boolean validateItem(ItemStack stack) {
        return !ModuleHelper.isRedstoneBehaviourEnabled(stack);
    }
}
