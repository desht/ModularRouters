package me.desht.modularrouters.recipe.enhancement;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class RedstoneEnhancementRecipe extends ModuleEnhancementRecipe {
    public RedstoneEnhancementRecipe(ItemModule.ModuleType type) {
        super(ModuleHelper.makeItemStack(type),
                " R ", "TMT", " R ",
                'R', Items.REDSTONE,
                'T', Blocks.REDSTONE_TORCH,
                'M', ModuleHelper.makeItemStack(type));
    }

    @Override
    public void applyEnhancement(ItemStack stack) {
        ModuleHelper.setRedstoneBehaviour(stack, true, RouterRedstoneBehaviour.ALWAYS);
    }

    @Override
    public String getRecipeId() {
        return "redstone";
    }

    @Override
    protected boolean validateModule(ItemStack stack) {
        return !ModuleHelper.isRedstoneBehaviourEnabled(stack);
    }
}
