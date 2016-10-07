package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.Map;

public class EnchantBreakerModuleRecipe extends ShapelessOreRecipe {
    private static final Enchantment[] validEnchantments = new Enchantment[] {
            Enchantments.SILK_TOUCH,
            Enchantments.FORTUNE
    };

    public EnchantBreakerModuleRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
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
                } else if (stack.isItemEqual(ItemModule.makeItemStack(ItemModule.ModuleType.BREAKER))) {
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
                } else if (stack.isItemEqual(ItemModule.makeItemStack(ItemModule.ModuleType.BREAKER)) && EnchantmentHelper.getEnchantments(stack).isEmpty()) {
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
