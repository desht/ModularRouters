package me.desht.modularrouters.recipe.enhancement;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class PickupDelayEnhancementRecipe extends ModuleEnhancementRecipe {
    public PickupDelayEnhancementRecipe(ItemModule.ModuleType type) {
        super(ItemModule.makeItemStack(type), "SM",
                'S', Items.SLIME_BALL,
                'M', ItemModule.makeItemStack(type));
    }

    @Override
    protected boolean validateModule(ItemStack stack) {
        return ItemModule.isType(stack, ItemModule.ModuleType.FLINGER) || ItemModule.isType(stack, ItemModule.ModuleType.DROPPER);
    }

    @Override
    public void applyEnhancement(ItemStack stack) {
        ModuleHelper.increasePickupDelay(stack);
    }

    @Override
    public String getRecipeId() {
        return "pickupDelay";
    }
}
