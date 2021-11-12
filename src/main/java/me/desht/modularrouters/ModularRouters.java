package me.desht.modularrouters;

import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.*;
import me.desht.modularrouters.datagen.*;
import me.desht.modularrouters.integration.IntegrationHandler;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod(ModularRouters.MODID)
public class ModularRouters {
    public static final String MODID = "modularrouters";
    public static final String MODNAME = "Modular Routers";

    public static final Logger LOGGER = LogManager.getLogger();

    public ModularRouters() {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ConfigHolder.init();

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSetup::initEarly);

        modBus.addListener(this::commonSetup);
        forgeBus.addListener(this::addReloadListeners);

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

        event.enqueueWork(() -> {
            IntegrationHandler.registerAll();
            XPCollection.detectXPTypes();
            ModNameCache.init();
        });
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CacheReloadListener());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class DataGenerators {
        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            DataGenerator generator = event.getGenerator();
            if (event.includeServer()) {
                generator.addProvider(new ModRecipeProvider(generator));
                generator.addProvider(new ModItemTagsProvider(generator, event.getExistingFileHelper()));
                generator.addProvider(new ModLootTableProvider(generator));
            }
            if (event.includeClient()) {
                generator.addProvider(new ModBlockStateProvider(generator, event.getExistingFileHelper()));
                generator.addProvider(new ModItemModelProvider(generator, event.getExistingFileHelper()));
            }
        }
    }

    public static class CacheReloadListener implements IFutureReloadListener {
        @Override
        public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return CompletableFuture.runAsync(CompiledActivatorModule::clearBlacklistCache, gameExecutor).thenCompose(stage::wait);
        }
    }
}
