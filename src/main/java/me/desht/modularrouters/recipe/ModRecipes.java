package me.desht.modularrouters.recipe;

import amerifrance.guideapi.api.GuideAPI;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.integration.guideapi.Guidebook;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModRecipes {
    public static void init() {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.itemRouter, 4),
                "ibi", "brb", "ibi",
                'b', Blocks.IRON_BARS, 'r', ModItems.blankModule, 'i', Items.IRON_INGOT);

        GameRegistry.addRecipe(new ItemStack(ModItems.blankModule, 6),
                " r ", "ppp", "nnn",
                'r', Items.REDSTONE, 'p', Items.PAPER, 'n', Items.GOLD_NUGGET);
        for (ItemModule.ModuleType type : ItemModule.ModuleType.values()) {
            GameRegistry.addRecipe(ItemModule.getModule(type).getRecipe());
        }

        GameRegistry.addRecipe(new ItemStack(ModItems.blankUpgrade, 6),
                "ppn", "pdn", " pn",
                'p', Items.PAPER, 'd', Items.DIAMOND, 'n', Items.GOLD_NUGGET);
        for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
            GameRegistry.addRecipe(ItemUpgrade.getUpgrade(type).getRecipe());
        }

        GameRegistry.addRecipe(new EnchantBreakerModuleRecipe());

        if (Loader.isModLoaded("guideapi")) {
            GameRegistry.addShapelessRecipe(GuideAPI.getStackFromBook(Guidebook.guideBook), Items.BOOK, ModItems.blankModule);
        }

        MinecraftForge.EVENT_BUS.register(ItemCraftedListener.class);
    }
}
