package me.desht.modularrouters;

import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.*;
import me.desht.modularrouters.datagen.*;
import me.desht.modularrouters.integration.IntegrationHandler;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.ModNameCache;
import me.desht.modularrouters.util.WildcardedRLMatcher;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(ModularRouters.MODID)
public class ModularRouters {
    public static final String MODID = "modularrouters";
    public static final String MODNAME = "Modular Routers";

    public static final Logger LOGGER = LogManager.getLogger(MODNAME);

    private static WildcardedRLMatcher dimensionBlacklist;

    public ModularRouters() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ConfigHolder.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.initEarly();
        }

        modBus.addListener(this::commonSetup);

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenuTypes.MENUS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModRecipes.RECIPES.register(modBus);
        ModCreativeModeTabs.TABS.register(modBus);
    }

    public static WildcardedRLMatcher getDimensionBlacklist() {
        if (dimensionBlacklist == null) {
            dimensionBlacklist = new WildcardedRLMatcher(ConfigHolder.common.module.dimensionBlacklist.get());
        }
        return dimensionBlacklist;
    }

    public static void clearDimensionBlacklist() {
        dimensionBlacklist = null;
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

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class DataGenerators {
        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            DataGenerator generator = event.getGenerator();
            CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
            ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

            generator.addProvider(event.includeServer(), new ModRecipeProvider(generator, lookupProvider));
            ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(generator, lookupProvider, existingFileHelper);
            generator.addProvider(event.includeServer(), blockTagsProvider);
            generator.addProvider(event.includeServer(), new ModItemTagsProvider(generator, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
            generator.addProvider(event.includeServer(), new ModLootTableProvider(generator));
            generator.addProvider(event.includeServer(), new ModEntityTypeTagsProvider(generator, lookupProvider, existingFileHelper));

            generator.addProvider(event.includeClient(), new ModBlockStateProvider(generator, existingFileHelper));
            generator.addProvider(event.includeClient(), new ModItemModelProvider(generator, existingFileHelper));
        }
    }
}
