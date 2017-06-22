package me.desht.modularrouters.recipe.enhancement;

import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class RangeUpRecipe extends ModuleEnhancementRecipe {
    public RangeUpRecipe(ItemModule.ModuleType type) {
        super(ModuleHelper.makeItemStack(type),
                " Q ", "QMQ", " S ",
                'Q', Items.QUARTZ, 'M', ModuleHelper.makeItemStack(type), 'S', "stickWood");
    }

    @Override
    protected boolean validateModule(ItemStack stack) {
        Module m = ItemModule.getModule(stack);
        return m instanceof IRangedModule && ((IRangedModule) m).getCurrentRange(stack) < ((IRangedModule) m).getHardMaxRange();
    }

    @Override
    public void applyEnhancement(ItemStack stack) {
        ModuleHelper.adjustRangeBoost(stack, 1);
    }

    @Override
    public String getRecipeId() {
        return "rangeUp";
    }
}
