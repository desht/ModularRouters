package me.desht.modularrouters;

import me.desht.modularrouters.item.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;

import javax.annotation.Nullable;
import java.util.Map;

public class EnchantBreakerModuleRecipe implements IRecipe {
    public static final Enchantment[] validEnchantments = new Enchantment[] {
            Enchantments.SILK_TOUCH,
            Enchantments.FORTUNE
    };

    static {
        RecipeSorter.register("ModularRouters:enchantBreakerRecipe", EnchantBreakerModuleRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        int nModules = 0;
        int nBooks = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null) {
                if (stack.getItem() == Items.ENCHANTED_BOOK && getValidEnchantment(stack) != null) {
                    nBooks++;
                } else if (stack.getItem() == ModItems.breakerModule) {
                    nModules++;
                } else {
                    return false;
                }
            }
        }
        return nModules == 1 && nBooks == 1;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack breakerModule = null;
        EnchantmentData ench = null;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null) {
                if (stack.getItem() == Items.ENCHANTED_BOOK) {
                    ench = getValidEnchantment(stack);
                } else if (stack.getItem() == ModItems.breakerModule && EnchantmentHelper.getEnchantments(stack).isEmpty()) {
                    breakerModule = stack.copy();
                }
            }
        }
        if (breakerModule != null && ench != null) {
            breakerModule.addEnchantment(ench.enchantmentobj, ench.enchantmentLevel);
            return breakerModule;
        } else {
            return null;
        }
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Nullable
    @Override
    public ItemStack getRecipeOutput() {
        return null;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        ItemStack[] aitemstack = new ItemStack[inv.getSizeInventory()];

        for (int i = 0; i < aitemstack.length; ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);
            aitemstack[i] = net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack);
        }

        return aitemstack;
    }

    private EnchantmentData getValidEnchantment(ItemStack stack) {
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        for (Enchantment ench : validEnchantments) {
            if (enchants.containsKey(ench)) {
                return new EnchantmentData(ench, enchants.get(ench));
            }
        }
        return null;
    }
}
