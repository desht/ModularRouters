package me.desht.modularrouters.recipe.enhancement;

import me.desht.modularrouters.item.module.ItemModule.ModuleType;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleResetRecipe extends ModuleEnhancementRecipe {
    public ModuleResetRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    protected boolean validateModule(ItemStack stack) {
        return true;
    }

    @Override
    public void applyEnhancement(ItemStack stack) {
        // reset the module's NBT
        stack.setTagCompound(new NBTTagCompound());
        ModuleHelper.validateNBT(stack);
    }

    @Override
    public String getRecipeId() {
        return "reset";
    }
}
