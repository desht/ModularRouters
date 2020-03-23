package me.desht.modularrouters.recipe;

import me.desht.modularrouters.core.ModRecipes;
import me.desht.modularrouters.item.module.BreakerModule;
import me.desht.modularrouters.item.module.ExtruderModule1;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class EnchantModuleRecipe extends SpecialRecipe {
    private static final Map<Class<?>, Enchantment[]> validEnchantments = new HashMap<>();

    static {
        validEnchantments.put(BreakerModule.class, new Enchantment[] {
                Enchantments.SILK_TOUCH,
                Enchantments.FORTUNE
        });
        validEnchantments.put(ExtruderModule1.class, new Enchantment[] {
                Enchantments.SILK_TOUCH,
        });
    }

    public EnchantModuleRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        ItemStack book = ItemStack.EMPTY;
        ItemStack module = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == Items.ENCHANTED_BOOK) {
                    book = stack.copy();
                } else if (stack.getItem() instanceof ItemModule) {
                    module = stack.copy();
                } else {
                    return false;
                }
            }
        }
        return !book.isEmpty() && !module.isEmpty()
                && EnchantmentHelper.getEnchantments(module).isEmpty()
                && getValidEnchantment(book, module) != null;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack book = ItemStack.EMPTY;
        ItemStack module = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() == Items.ENCHANTED_BOOK) {
                book = stack;
            } else if (stack.getItem() instanceof ItemModule) {
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

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.MODULE_ENCHANT.get();
    }

    private EnchantmentData getValidEnchantment(ItemStack bookStack, ItemStack moduleStack) {
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(bookStack);
        Enchantment[] valid = validEnchantments.get(moduleStack.getItem().getClass());
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
