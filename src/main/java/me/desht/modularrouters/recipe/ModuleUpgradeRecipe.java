package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

public abstract class ModuleUpgradeRecipe extends ShapedOreRecipe {
    public ModuleUpgradeRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (ItemModule.getModule(stack) != null && !validateItem(stack)) {
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
            if (stack != null && stack.getItem() instanceof ItemModule) {
                compound = stack.getTagCompound();
                break;
            }
        }
        if (compound != null) {
            out.setTagCompound(compound.copy());
            enableUpgrade(out);
        }
        return out;
    }

    /**
     * Check that the item is OK for an upgrade to be applied.
     *
     * @param stack the item to check
     * @return true if the item is OK for upgrading
     */
    protected abstract boolean validateItem(ItemStack stack);

    /**
     * Do what's necessary to the item to enable the upgrade, generally modifying the item's NBT in some way.
     * The item is guaranteed to have some NBT when this is called, i.e. getTagCompound() will not return null.
     *
     * @param stack the item to modify
     */
    public abstract void enableUpgrade(ItemStack stack);
}
