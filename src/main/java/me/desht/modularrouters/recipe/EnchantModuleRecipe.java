package me.desht.modularrouters.recipe;

import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.HashMap;
import java.util.Map;

class EnchantModuleRecipe extends ShapelessOreRecipe {
    static final Map<ItemModule.ModuleType, Enchantment[]> validEnchantments = new HashMap<>();

    static {
        validEnchantments.put(ItemModule.ModuleType.BREAKER, new Enchantment[] {
                Enchantments.SILK_TOUCH,
                Enchantments.FORTUNE
        });
        validEnchantments.put(ItemModule.ModuleType.EXTRUDER, new Enchantment[] {
                Enchantments.SILK_TOUCH,
        });
    }

    EnchantModuleRecipe(String name, ItemStack result, Object... recipe) {
        super(MiscUtil.RL(name), result, recipe);
        setRegistryName(MiscUtil.RL(name));
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        ItemStack book = ItemStack.EMPTY;
        ItemStack module = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == Items.ENCHANTED_BOOK) {
                    book = stack.copy();
                } else if (stack.getItem() == RegistrarMR.MODULE) {
                    module = stack.copy();
                } else {
                    return false;
                }
            }
        }
        return !book.isEmpty() && !module.isEmpty() && EnchantmentHelper.getEnchantments(module).isEmpty() && getValidEnchantment(book, module) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack book = ItemStack.EMPTY;
        ItemStack module = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() == Items.ENCHANTED_BOOK) {
                book = stack;
            } else if (stack.getItem() == RegistrarMR.MODULE) {
                module = stack.copy();
            }
        }
        if (!book.isEmpty() && !module.isEmpty()) {
            EnchantmentData ench = getValidEnchantment(book, module);
            if (ench != null) {
                ItemStack result = module.copy();
                result.addEnchantment(ench.enchantment, ench.enchantmentLevel);
                return result;
            }
        }

        return ItemStack.EMPTY;
    }

    private EnchantmentData getValidEnchantment(ItemStack bookStack, ItemStack moduleStack) {
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(bookStack);
        Enchantment[] valid = validEnchantments.get(ItemModule.ModuleType.getType(moduleStack));
        if (valid != null) {
            for (Enchantment ench : valid) {
                if (enchants.containsKey(ench)) {
                    return new EnchantmentData(ench, enchants.get(ench));
                }
            }
        }
        return null;
    }
}
