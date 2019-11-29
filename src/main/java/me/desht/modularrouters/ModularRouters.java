package me.desht.modularrouters;

import me.desht.modularrouters.client.Keybindings;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.ScreenFactoryRegistration;
import me.desht.modularrouters.client.item_beam.ItemBeamDispatcher;
import me.desht.modularrouters.client.model.ModelBakeEventHandler;
import me.desht.modularrouters.client.render.area.AreaShowManager;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.integration.IntegrationHandler;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.proxy.ClientProxy;
import me.desht.modularrouters.proxy.IProxy;
import me.desht.modularrouters.proxy.ServerProxy;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("modularrouters")
public class ModularRouters {
    public static final String MODID = "modularrouters";
    public static final String MODNAME = "Modular Routers";

    public static final Logger LOGGER = LogManager.getLogger();

    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public ModularRouters() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientHandler::clientSetup);
        });

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info(MODNAME + " is loading!");

        PacketHandler.setupNetwork();

        DeferredWorkQueue.runLater(() -> {
            IntegrationHandler.registerAll();
            XPCollection.detectXPTypes();
            ModNameCache.init();
        });
    }

    static class ClientHandler {
        static void clientSetup(FMLClientSetupEvent event) {
            FMLJavaModLoadingContext.get().getModEventBus().register(ModelBakeEventHandler.class);
            MinecraftForge.EVENT_BUS.register(AreaShowManager.INSTANCE);
            MinecraftForge.EVENT_BUS.register(ItemBeamDispatcher.INSTANCE);
            MinecraftForge.EVENT_BUS.register(MouseOverHelp.class);

            DeferredWorkQueue.runLater(() -> {
                ScreenFactoryRegistration.registerScreenFactories();
                Keybindings.registerKeyBindings();
            });
        }
    }
}
