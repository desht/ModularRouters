package me.desht.modularrouters.recipe;

import amerifrance.guideapi.api.GuideAPI;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.integration.guideapi.Guidebook;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleType;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;

public class ModRecipes {
    public static void init() {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.itemRouter, 4),
                "ibi", "brb", "ibi",
                'b', Blocks.IRON_BARS, 'r', ModItems.blankModule, 'i', Items.IRON_INGOT);

        GameRegistry.addRecipe(new ItemStack(ModItems.blankModule, 6),
                " r ", "ppp", "nnn",
                'r', Items.REDSTONE, 'p', Items.PAPER, 'n', Items.GOLD_NUGGET);
        for (ModuleType type : ModuleType.values()) {
            IRecipe recipe = ItemModule.getModule(type).getRecipe();
            if (recipe != null) GameRegistry.addRecipe(recipe);
        }

        GameRegistry.addRecipe(new ItemStack(ModItems.blankUpgrade, 6),
                "ppn", "pdn", " pn",
                'p', Items.PAPER, 'd', Items.DIAMOND, 'n', Items.GOLD_NUGGET);
        for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
            GameRegistry.addRecipe(ItemUpgrade.getUpgrade(type).getRecipe());
        }

        for (ItemSmartFilter.FilterType type : ItemSmartFilter.FilterType.values()) {
            GameRegistry.addRecipe(ItemSmartFilter.getFilter(type).getRecipe());
        }
        // special case for deprecated mod sorter module
        GameRegistry.addShapelessRecipe(
                ItemSmartFilter.makeItemStack(ItemSmartFilter.FilterType.MOD),
                ItemModule.makeItemStack(ModuleType.MODSORTER));

        RecipeSorter.register(ModularRouters.modId + ":enchantBreaker", EnchantBreakerModuleRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
        GameRegistry.addRecipe(new EnchantBreakerModuleRecipe(ItemModule.makeItemStack(ModuleType.BREAKER), ItemModule.makeItemStack(ModuleType.BREAKER), Items.ENCHANTED_BOOK));

        RecipeSorter.register(ModularRouters.modId + ":redstoneUpgrade", RedstoneUpgradeRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        for (ModuleType type : ModuleType.values()) {
            ItemStack output = ItemModule.makeItemStack(type);
            Module.validateNBT(output);
            RedstoneUpgradeRecipe.addRedstoneNbt(output.getTagCompound());
            GameRegistry.addRecipe(new RedstoneUpgradeRecipe(output,
                    " R ", "TMT",
                    'R', Items.REDSTONE,
                    'T', Blocks.REDSTONE_TORCH,
                    'M', ItemModule.makeItemStack(type)));
        }

        if (Loader.isModLoaded("guideapi")) {
            GameRegistry.addShapelessRecipe(GuideAPI.getStackFromBook(Guidebook.guideBook), Items.BOOK, ModItems.blankModule);
        }

        MinecraftForge.EVENT_BUS.register(ItemCraftedListener.class);
    }
}
