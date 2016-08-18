package me.desht.modularrouters.recipe;

import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModRecipes {
    public static void init() {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.itemRouter, 4),
                "ibi", "brb", "ibi",
                'b', Blocks.IRON_BARS, 'r', ModItems.blankModule, 'i', Items.IRON_INGOT);

        GameRegistry.addRecipe(new ItemStack(ModItems.blankModule, 6),
                " r ", "ppp", "nnn",
                'r', Items.REDSTONE, 'p', Items.PAPER, 'n', Items.GOLD_NUGGET);

        GameRegistry.addShapelessRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.BREAKER),
                ModItems.blankModule, Items.IRON_PICKAXE);
        GameRegistry.addShapelessRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.DROPPER),
                ModItems.blankModule, Blocks.DROPPER);
        GameRegistry.addShapelessRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.PLACER),
                ModItems.blankModule, Blocks.DISPENSER, Blocks.DIRT);
        GameRegistry.addShapelessRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.SORTER),
                ItemModule.makeItemStack(ItemModule.ModuleType.DETECTOR), ItemModule.makeItemStack(ItemModule.ModuleType.SENDER1));
        GameRegistry.addShapelessRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.PULLER),
                ModItems.blankModule, Blocks.STICKY_PISTON);
        GameRegistry.addShapelessRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.SENDER1),
                ModItems.blankModule, Items.BOW, Items.ARROW);
        GameRegistry.addShapelessRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.SENDER2),
                ItemModule.makeItemStack(ItemModule.ModuleType.SENDER1), Items.ENDER_EYE);
        GameRegistry.addShapelessRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.SENDER3),
                ItemModule.makeItemStack(ItemModule.ModuleType.SENDER2), Blocks.END_STONE, Blocks.ENDER_CHEST);
        GameRegistry.addShapelessRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.VACUUM),
                ModItems.blankModule, Blocks.HOPPER, Items.ENDER_EYE);
        GameRegistry.addShapelessRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.VOID),
                ModItems.blankModule, Items.LAVA_BUCKET);
        GameRegistry.addShapelessRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.DETECTOR),
                ModItems.blankModule, Items.COMPARATOR);

        GameRegistry.addRecipe(new ItemStack(ModItems.blankUpgrade, 4),
                "ppn", "pdn", " pn",
                'p', Items.PAPER, 'd', Items.DIAMOND, 'n', Items.GOLD_NUGGET);

        GameRegistry.addShapelessRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.SPEED),
                ModItems.blankUpgrade, Items.BLAZE_POWDER, Items.SUGAR, Items.GUNPOWDER, Items.REDSTONE);
        GameRegistry.addShapelessRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.STACK),
                ModItems.blankUpgrade, Blocks.BRICK_BLOCK, Blocks.STONEBRICK);
        GameRegistry.addShapelessRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.RANGE),
                ModItems.blankUpgrade, Items.PRISMARINE_SHARD);

        GameRegistry.addRecipe(new EnchantBreakerModuleRecipe());
    }
}
