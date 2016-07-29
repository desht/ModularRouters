package me.desht.modularrouters.proxy;

import me.desht.modularrouters.EnchantBreakerModuleRecipe;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.network.ModuleSettingsMessage;
import me.desht.modularrouters.network.ParticleMessage;
import me.desht.modularrouters.network.RouterSettingsMessage;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {
    public static SimpleNetworkWrapper network;

    public void registerItemRenderer(Item item, int meta, String id) {
        // STUB
    }

    public void preInit() {
        Config.preInit();
        ModItems.init();
        ModBlocks.init();

        network = NetworkRegistry.INSTANCE.newSimpleChannel(ModularRouters.modId);
        network.registerMessage(ModuleSettingsMessage.Handler.class, ModuleSettingsMessage.class, 0, Side.SERVER);
        network.registerMessage(RouterSettingsMessage.Handler.class, RouterSettingsMessage.class, 1, Side.SERVER);
        network.registerMessage(ParticleMessage.Handler.class, ParticleMessage.class, 2, Side.CLIENT);

        GameRegistry.registerTileEntity(TileEntityItemRouter.class, "item_router");
    }

    public void init() {
        NetworkRegistry.INSTANCE.registerGuiHandler(ModularRouters.instance, new GuiProxy());
        setupRecipes();
    }

    public void postInit() {

    }

    private void setupRecipes() {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.itemRouter, 4),
                "ibi", "brb", "ibi",
                'b', Blocks.IRON_BARS, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT);

        GameRegistry.addRecipe(new ItemStack(ModItems.blankModule, 3),
                " r ", "ppp", "nnn",
                'r', Items.REDSTONE, 'p', Items.PAPER, 'n', Items.GOLD_NUGGET);

        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.breakerModule),
                ModItems.blankModule, Items.IRON_PICKAXE);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.dropperModule),
                ModItems.blankModule, Blocks.DROPPER);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.placerModule),
                ModItems.blankModule, Blocks.DISPENSER, Blocks.DIRT);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.sorterModule),
                ModItems.blankModule, Items.COMPARATOR, Items.SPIDER_EYE);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.pullerModule),
                ModItems.blankModule, Blocks.STICKY_PISTON);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.receiverModule),
                ModItems.blankModule, Blocks.TRAPDOOR);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.senderModule1),
                ModItems.blankModule, Items.BOW, Items.ARROW);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.senderModule2),
                ModItems.senderModule1, Items.ENDER_EYE);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.senderModule3),
                ModItems.senderModule2, Blocks.END_STONE, Blocks.ENDER_CHEST);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.vacuumModule),
                ModItems.blankModule, Blocks.HOPPER, Items.ENDER_EYE);

        GameRegistry.addRecipe(new ItemStack(ModItems.blankUpgrade, 4),
                "ppn", "pdn", " pn",
                'p', Items.PAPER, 'd', Items.DIAMOND, 'n', Items.GOLD_NUGGET);
        GameRegistry.addShapelessRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.SPEED),
                ModItems.blankUpgrade, Items.BLAZE_POWDER, Items.SUGAR, Items.GUNPOWDER, Items.REDSTONE);
        GameRegistry.addShapelessRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.STACK),
                ModItems.blankUpgrade, Blocks.BRICK_BLOCK, Blocks.STONEBRICK);
        GameRegistry.addShapelessRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.RANGE),
                ModItems.blankUpgrade, Items.ENDER_PEARL, Items.PRISMARINE_SHARD);

//        for (Enchantment ench : EnchantBreakerModuleRecipe.validEnchantments) {
//            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
//            book.addEnchantment(ench, 1);
//        }
        GameRegistry.addRecipe(new EnchantBreakerModuleRecipe());
    }
}
