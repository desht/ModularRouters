package me.desht.modularrouters;

import me.desht.modularrouters.client.AreaShowManager;
import me.desht.modularrouters.client.Keybindings;
import me.desht.modularrouters.client.ModelBakeEventHandler;
import me.desht.modularrouters.client.fx.RenderListener;
import me.desht.modularrouters.client.gui.GuiHandler;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.integration.IntegrationHandler;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.proxy.ClientProxy;
import me.desht.modularrouters.proxy.IProxy;
import me.desht.modularrouters.proxy.ServerProxy;
import me.desht.modularrouters.recipe.ModRecipes;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod("modularrouters")
public class ModularRouters {
    public static final String MODID = "modularrouters";
    public static final String MODNAME = "Modular Routers";
    static final String MODVERSION = "@VERSION@";
//    static final String DEPENDENICES =
//            "after:waila;before:guideapi@[1.12-2.1.4-56,);after:theoneprobe;"
//                    + "required-after:forge@[14.23.4.2705,);";

    public static final Logger LOGGER = LogManager.getLogger();

    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public ModularRouters() {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> GuiHandler::openGui);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientHandler::clientSetup);
            MinecraftForge.EVENT_BUS.addListener(ClientHandler::registerRenders);
        });
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info(MODNAME + " is loading!");

        PacketHandler.setupNetwork();

        DeferredWorkQueue.runLater(() -> {
            ModRecipes.init(); // todo 1.13 should be unnecessary...
            IntegrationHandler.registerAll();
            XPCollection.detectXPFluids();
            ModNameCache.init();
        });
    }

    static class ClientHandler {
        static void clientSetup(FMLClientSetupEvent event) {
            MinecraftForge.EVENT_BUS.register(ModelBakeEventHandler.class);
            MinecraftForge.EVENT_BUS.register(AreaShowManager.getInstance());
            MinecraftForge.EVENT_BUS.register(MouseOverHelp.class);
            MinecraftForge.EVENT_BUS.register(RenderListener.class);

            Keybindings.registerKeyBindings();
        }

        static void registerRenders(ModelRegistryEvent event) {
            // todo 1.13 what do we need here?
        }
    }
}
