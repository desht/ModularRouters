package me.desht.modularrouters.recipe;

import com.google.common.base.Joiner;
import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleType;
import me.desht.modularrouters.recipe.enhancement.*;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class ModRecipes {
    public static void init() {
        addEnchantmentRecipes();
        addRedstoneUpgradeRecipes();
        addRegulatorUpgradeRecipes();
        addPickupDelayRecipes();
        addFastPickupRecipe();
        addXPVacuumRecipe();
        addRangeRecipes();
        addSelfCraftRecipes();

        MinecraftForge.EVENT_BUS.register(ItemCraftedListener.class);
    }

    private static void addEnchantmentRecipes() {
        for (ModuleType type : EnchantModuleRecipe.validEnchantments.keySet()) {
            for (Enchantment ench : EnchantModuleRecipe.validEnchantments.get(type)) {
                for (int level = ench.getMinLevel(); level <= ench.getMaxLevel(); level++) {
                    ItemStack resStack = ModuleHelper.makeItemStack(type);
                    ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                    resStack.addEnchantment(ench, level);
                    book.addEnchantment(ench, level);
                    ForgeRegistries.RECIPES.register(new EnchantModuleRecipe(
                            Joiner.on("_").join(type.name(), ench.getName(), level),
                            resStack, ModuleHelper.makeItemStack(type), book));
                }
            }
        }
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

    private static void addFastPickupRecipe() {
        FastPickupEnhancementRecipe recipe = new FastPickupEnhancementRecipe(ModuleType.VACUUM);
        ForgeRegistries.RECIPES.register(recipe.setRegistryName(RL(ModuleType.VACUUM + "_" + "fast_pickup")));
    }

    private static void addRedstoneUpgradeRecipes() {
        for (ModuleType type : ModuleType.values()) {
            RedstoneEnhancementRecipe recipe = new RedstoneEnhancementRecipe(type);
            ForgeRegistries.RECIPES.register(recipe.setRegistryName(RL(type + "_" + "redstone")));
        }
    }

    private static void addRegulatorUpgradeRecipes() {
        for (ModuleType type : ModuleType.values()) {
            if (RegulatorEnhancementRecipe.appliesTo(type)) {
                RegulatorEnhancementRecipe recipe = new RegulatorEnhancementRecipe(type);
                ForgeRegistries.RECIPES.register(recipe.setRegistryName(type + "_" + "regulator"));
            }
        }
    }

    private static void addPickupDelayRecipes() {
        for (ModuleType type : new ItemModule.ModuleType[]{ModuleType.DROPPER, ModuleType.FLINGER}) {
            PickupDelayEnhancementRecipe recipe = new PickupDelayEnhancementRecipe(type);
            ForgeRegistries.RECIPES.register(recipe.setRegistryName(RL(type + "_" + "pickup_delay")));
        }
    }

    private static void addXPVacuumRecipe() {
        XPVacuumEnhancementRecipe recipe = new XPVacuumEnhancementRecipe(ModuleType.VACUUM);
        ForgeRegistries.RECIPES.register(recipe.setRegistryName(ModuleType.VACUUM + "_" + "xp_vacuum"));
    }

    private static void addRangeRecipes() {
        for (ModuleType type : ModuleType.values()) {
            if (ItemModule.getModule(type) instanceof IRangedModule) {
                RangeUpRecipe recipeUp = new RangeUpRecipe(type);
                ForgeRegistries.RECIPES.register(recipeUp.setRegistryName(RL(type + "_" + "range_up")));
                RangeDownRecipe recipeDown = new RangeDownRecipe(type);
                ForgeRegistries.RECIPES.register(recipeDown.setRegistryName(RL(type + "_" + "range_down")));
            }
        }
    }
}
