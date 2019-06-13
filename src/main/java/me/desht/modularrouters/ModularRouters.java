package me.desht.modularrouters;

import me.desht.modularrouters.client.AreaShowManager;
import me.desht.modularrouters.client.ColorHandlers;
import me.desht.modularrouters.client.Keybindings;
import me.desht.modularrouters.client.ModelBakeEventHandler;
import me.desht.modularrouters.client.fx.RenderListener;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.integration.IntegrationHandler;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.proxy.ClientProxy;
import me.desht.modularrouters.proxy.IProxy;
import me.desht.modularrouters.proxy.ServerProxy;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
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
    static final String MODVERSION = "@VERSION@";

    public static final Logger LOGGER = LogManager.getLogger();

    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public ModularRouters() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigHandler.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientHandler::clientSetup);
            MinecraftForge.EVENT_BUS.addListener(ClientHandler::registerRenders);
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
            MinecraftForge.EVENT_BUS.register(ModelBakeEventHandler.class);
            MinecraftForge.EVENT_BUS.register(AreaShowManager.getInstance());
            MinecraftForge.EVENT_BUS.register(MouseOverHelp.class);
            MinecraftForge.EVENT_BUS.register(RenderListener.class);
            MinecraftForge.EVENT_BUS.register(ColorHandlers.class);

            ModContainerTypes.registerScreenFactories();

            Keybindings.registerKeyBindings();
        }

        static void registerRenders(ModelRegistryEvent event) {
            // todo 1.14 what do we need here?
        }
    }
}
