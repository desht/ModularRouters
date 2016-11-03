package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class RedstoneUpgradeRecipe extends ModuleUpgradeRecipe {
    RedstoneUpgradeRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    public void enableUpgrade(ItemStack stack) {
        ModuleHelper.setRedstoneBehaviour(stack, true, RouterRedstoneBehaviour.ALWAYS);
    }

    @Override
    protected boolean validateItem(ItemStack stack) {
        return !ModuleHelper.isRedstoneBehaviourEnabled(stack);
    }
}
