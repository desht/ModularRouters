package me.desht.modularrouters.recipe.enhancement;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class XPVacuumEnhancementRecipe extends ModuleEnhancementRecipe {
    public XPVacuumEnhancementRecipe(ItemModule.ModuleType type) {
        super(ModuleHelper.makeItemStack(type),
                "SM",
                'S', Blocks.SOUL_SAND,
                'M', ModuleHelper.makeItemStack(type));
    }

    @Override
    protected boolean validateModule(ItemStack stack) {
        return ModuleHelper.isModuleType(stack, ItemModule.ModuleType.VACUUM) && !ModuleHelper.hasXPVacuum(stack);
    }

    @Override
    public void applyEnhancement(ItemStack stack) {
        ModuleHelper.enableXPVacuum(stack);
    }

    @Override
    public String getRecipeId() {
        return "xpVacuum";
    }
}
