package me.desht.modularrouters.recipe.enhancement;

import me.desht.modularrouters.item.module.ItemModule.ModuleType;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class FastPickupEnhancementRecipe extends ModuleEnhancementRecipe {
    public FastPickupEnhancementRecipe(ModuleType type) {
        super(ModuleHelper.makeItemStack(type),
                "FM",
                'F', Items.FISHING_ROD,
                'M', ModuleHelper.makeItemStack(type));
    }

    @Override
    protected boolean validateModule(ItemStack stack) {
        return ModuleHelper.isModuleType(stack, ModuleType.VACUUM) && !ModuleHelper.hasFastPickup(stack);
    }

    @Override
    public void applyEnhancement(ItemStack stack) {
        ModuleHelper.addFastPickup(stack);
    }

    @Override
    public String getRecipeId() {
        return "fastPickup";
    }
}
