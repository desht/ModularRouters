package me.desht.modularrouters.recipe.enhancement;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class RegulatorEnhancementRecipe extends ModuleEnhancementRecipe {
    public RegulatorEnhancementRecipe(ItemModule.ModuleType type) {
        super(ItemModule.makeItemStack(type),  " Q ", "CMC", " Q ",
                'Q', Items.QUARTZ,
                'C', Items.COMPARATOR,
                'M', ItemModule.makeItemStack(type));
    }

    @Override
    public void applyEnhancement(ItemStack stack) {
        ModuleHelper.setRegulatorAmount(stack, true, 1);
    }

    @Override
    public String getRecipeId() {
        return "regulator";
    }

    public static boolean appliesTo(ItemModule.ModuleType type) {
        return ItemModule.getModule(type).canBeRegulated();
    }

    @Override
    protected boolean validateModule(ItemStack stack) {
        return !ModuleHelper.isRegulatorEnabled(stack);
    }
}
