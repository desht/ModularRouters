package me.desht.modularrouters.recipe.enhancement;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class PickupDelayEnhancementRecipe extends ModuleEnhancementRecipe {
    public PickupDelayEnhancementRecipe(ItemModule.ModuleType type) {
        super(ModuleHelper.makeItemStack(type), "SM",
                'S', "slimeball",
                'M', ModuleHelper.makeItemStack(type));
    }

    @Override
    protected boolean validateModule(ItemStack stack) {
        return ModuleHelper.isModuleType(stack, ItemModule.ModuleType.FLINGER, ItemModule.ModuleType.DROPPER);
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
