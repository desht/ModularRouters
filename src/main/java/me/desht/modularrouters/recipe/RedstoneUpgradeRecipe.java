package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RedstoneUpgradeRecipe extends ShapedOreRecipe {
    public RedstoneUpgradeRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting var1) {
        return output(output, var1);
    }

    private static ItemStack output(ItemStack output, InventoryCrafting var1) {
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
            compound.setBoolean(Module.NBT_REDSTONE_ENABLED, true);
            compound.setString(Module.NBT_REDSTONE_MODE, RouterRedstoneBehaviour.ALWAYS.toString());
            out.setTagCompound(compound);
        }
        return out;
    }
}
