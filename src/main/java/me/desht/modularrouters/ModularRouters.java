package me.desht.modularrouters;

import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.Keybindings;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.model.ModelBakeEventHandler;
import me.desht.modularrouters.client.render.area.ModuleTargetRenderer;
import me.desht.modularrouters.client.render.item_beam.ItemBeamDispatcher;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.*;
import me.desht.modularrouters.datagen.ModItemTagsProvider;
import me.desht.modularrouters.datagen.ModLootTableProvider;
import me.desht.modularrouters.datagen.ModRecipeProvider;
import me.desht.modularrouters.integration.IntegrationHandler;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("modularrouters")
public class ModularRouters {
    public static final String MODID = "modularrouters";
    public static final String MODNAME = "Modular Routers";

    public static final Logger LOGGER = LogManager.getLogger();

    public ModularRouters() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ConfigHolder.init();

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> modBus.addListener(ClientHandler::clientSetup));

        modBus.addListener(this::commonSetup);

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModTileEntities.TILE_ENTITIES.register(modBus);
        ModContainerTypes.CONTAINERS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModRecipes.RECIPES.register(modBus);
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
            MinecraftForge.EVENT_BUS.register(ModuleTargetRenderer.class);
            MinecraftForge.EVENT_BUS.register(ItemBeamDispatcher.INSTANCE);
            MinecraftForge.EVENT_BUS.register(MouseOverHelp.class);

            DeferredWorkQueue.runLater(() -> {
                ClientSetup.init();
                Keybindings.registerKeyBindings();
            });
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class DataGenerators {
        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            DataGenerator generator = event.getGenerator();
            if (event.includeServer()) {
                generator.addProvider(new ModRecipeProvider(generator));
                generator.addProvider(new ModItemTagsProvider(generator));
                generator.addProvider(new ModLootTableProvider(generator));
            }
        }
    }
}
