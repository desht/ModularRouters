package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleType;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleResetRecipe extends ModuleEnhancementRecipe {
    ModuleResetRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    protected boolean validateItem(ItemStack stack) {
        return !ItemModule.isType(stack, ModuleType.SORTER) && !ItemModule.isType(stack , ModuleType.MODSORTER);
    }

    @Override
    public void enableUpgrade(ItemStack stack) {
        // reset the module's NBT
        stack.setTagCompound(new NBTTagCompound());
        ModuleHelper.validateNBT(stack);
    }

    @Override
    public String getRecipeId() {
        return "reset";
    }
}
