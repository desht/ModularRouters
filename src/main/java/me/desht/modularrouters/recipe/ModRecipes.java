package me.desht.modularrouters.recipe;

import amerifrance.guideapi.api.GuideAPI;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.integration.guideapi.Guidebook;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleType;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.ModuleHelper;
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

        ItemStack lapis = new ItemStack(Items.DYE, 1, 4);
        GameRegistry.addRecipe(new ItemStack(ModItems.blankUpgrade, 6),
                "ppn", "pdn", " pn",
                'p', Items.PAPER, 'd', lapis, 'n', Items.GOLD_NUGGET);
        for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
            GameRegistry.addRecipe(ItemUpgrade.getUpgrade(type).getRecipe());
        }

        for (ItemSmartFilter.FilterType type : ItemSmartFilter.FilterType.values()) {
            GameRegistry.addRecipe(ItemSmartFilter.getFilter(type).getRecipe());
        }
        // special case for deprecated sorter & mod sorter modules
        GameRegistry.addShapelessRecipe(
                ItemSmartFilter.makeItemStack(ItemSmartFilter.FilterType.BULKITEM),
                ItemModule.makeItemStack(ModuleType.SORTER));
        GameRegistry.addShapelessRecipe(
                ItemSmartFilter.makeItemStack(ItemSmartFilter.FilterType.MOD),
                ItemModule.makeItemStack(ModuleType.MODSORTER));

        RecipeSorter.register(ModularRouters.modId + ":enchantModule", EnchantModuleRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
        for (ModuleType type : EnchantModuleRecipe.validEnchantments.keySet()) {
            GameRegistry.addRecipe(new EnchantModuleRecipe(ItemModule.makeItemStack(type), ItemModule.makeItemStack(type), Items.ENCHANTED_BOOK));
        }

        addRedstoneUpgradeRecipes();
        addRegulatorUpgradeRecipes();

        if (Loader.isModLoaded("guideapi")) {
            GameRegistry.addShapelessRecipe(GuideAPI.getStackFromBook(Guidebook.guideBook), Items.BOOK, ModItems.blankModule);
        }

        MinecraftForge.EVENT_BUS.register(ItemCraftedListener.class);
    }

    private static void addRedstoneUpgradeRecipes() {
        RecipeSorter.register(ModularRouters.modId + ":redstoneUpgrade", RedstoneEnhancementRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        for (ModuleType type : ModuleType.values()) {
            ItemStack output = ItemModule.makeItemStack(type);
            ModuleHelper.setRedstoneBehaviour(output, true, RouterRedstoneBehaviour.ALWAYS);
            GameRegistry.addRecipe(new RedstoneEnhancementRecipe(output,
                    " R ", "TMT", " R ",
                    'R', Items.REDSTONE,
                    'T', Blocks.REDSTONE_TORCH,
                    'M', ItemModule.makeItemStack(type)));
        }
    }

    private static void addRegulatorUpgradeRecipes() {
        RecipeSorter.register(ModularRouters.modId + ":regulatorUpgrade", RegulatorEnhancementRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        for (ModuleType type : ModuleType.values()) {
            if (RegulatorEnhancementRecipe.appliesTo(type)) {
                ItemStack output = ItemModule.makeItemStack(type);
                ModuleHelper.setRegulatorAmount(output, true, 1);
                GameRegistry.addRecipe(new RegulatorEnhancementRecipe(output,
                        " Q ", "CMC", " Q ",
                        'Q', Items.QUARTZ,
                        'C', Items.COMPARATOR,
                        'M', ItemModule.makeItemStack(type)));
            }
        }
    }
}
