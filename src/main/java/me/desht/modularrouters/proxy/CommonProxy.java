package me.desht.modularrouters.proxy;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.integration.IntegrationHandler;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.network.*;
import me.desht.modularrouters.recipe.ModRecipes;
import net.minecraft.item.Item;
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

        int d = 0;
        network = NetworkRegistry.INSTANCE.newSimpleChannel(ModularRouters.modId);
        network.registerMessage(ModuleSettingsMessage.Handler.class, ModuleSettingsMessage.class, d++, Side.SERVER);
        network.registerMessage(RouterSettingsMessage.Handler.class, RouterSettingsMessage.class, d++, Side.SERVER);
        network.registerMessage(ParticleMessage.Handler.class, ParticleMessage.class, d++, Side.CLIENT);
        network.registerMessage(ModuleConfigMessage.Handler.class, ModuleConfigMessage.class, d++, Side.SERVER);
        network.registerMessage(ReopenRouterMessage.Handler.class, ReopenRouterMessage.class, d++, Side.SERVER);

        GameRegistry.registerTileEntity(TileEntityItemRouter.class, "item_router");
    }

    public void init() {
        NetworkRegistry.INSTANCE.registerGuiHandler(ModularRouters.instance, new GuiProxy());
        ModRecipes.init();
        IntegrationHandler.registerWaila();
        IntegrationHandler.registerTOP();
    }

    public void postInit() {

    }

}
