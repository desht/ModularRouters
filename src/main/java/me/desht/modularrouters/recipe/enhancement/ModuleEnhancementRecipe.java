package me.desht.modularrouters.recipe.enhancement;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

public abstract class ModuleEnhancementRecipe extends ShapedOreRecipe {
    ModuleEnhancementRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
//        applyEnhancement(result);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (ItemModule.getModule(stack) != null && !validateModule(stack)) {
                return false;
            }
        }
        return super.matches(inv, world);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting var1) {
        ItemStack out = output.copy();
        NBTTagCompound compound = null;
        for (int i = 0; i < var1.getSizeInventory(); i++) {
            ItemStack stack = var1.getStackInSlot(i);
            if (ItemModule.getModule(stack) != null) {
                compound = stack.getTagCompound();
                break;
            }
        }
        if (compound != null) {
            out.setTagCompound(compound.copy());
            applyEnhancement(out);
        }
        return out;
    }

    /**
     * Check that the output item is OK for an enhancement to be applied.
     *
     * @param stack the item to check
     * @return true if the item is OK for enhancement
     */
    protected abstract boolean validateModule(ItemStack stack);

    /**
     * Do what's necessary to the item to enable the enhancement, generally modifying the item's NBT in some way.
     * The item is guaranteed to have some NBT when this is called, i.e. getTagCompound() will not return null.
     *
     * @param stack the item to modify
     */
    public abstract void applyEnhancement(ItemStack stack);

    /**
     * Return a simple identifier for the recipe.  Used mainly for documentation/translation purposes (e.g. JEI)
     *
     * @return the recipe ID
     */
    public abstract String getRecipeId();
}
