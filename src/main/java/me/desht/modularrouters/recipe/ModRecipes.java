package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.module.ItemModule.ModuleType;
import me.desht.modularrouters.recipe.enhancement.ModuleResetRecipe;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class ModRecipes {
    public static void init() {
        addSelfCraftRecipes();
    }

    private static void addSelfCraftRecipes() {
        // crafting a module into itself resets all NBT on the module
        for (ModuleType type : ModuleType.values()) {
            ItemStack stack = ModuleHelper.makeItemStack(type);
            ItemStack output = ModuleHelper.makeItemStack(type);
            ModuleResetRecipe recipe = new ModuleResetRecipe(output, "M", 'M', stack);
            ForgeRegistries.RECIPES.register(recipe.setRegistryName(RL(type + "_" + "reset")));
        }
    }
}
