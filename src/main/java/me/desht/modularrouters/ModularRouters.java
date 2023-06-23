package me.desht.modularrouters;

import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.*;
import me.desht.modularrouters.datagen.*;
import me.desht.modularrouters.integration.IntegrationHandler;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(ModularRouters.MODID)
public class ModularRouters {
    public static final String MODID = "modularrouters";
    public static final String MODNAME = "Modular Routers";

    public static final Logger LOGGER = LogManager.getLogger(MODNAME);

    public ModularRouters() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ConfigHolder.init();

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSetup::initEarly);

        modBus.addListener(this::commonSetup);

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenuTypes.MENUS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModRecipes.RECIPES.register(modBus);
        ModCreativeModeTabs.TABS.register(modBus);
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
            if (event.includeServer()) {
                generator.addProvider(event.includeClient(), new ModRecipeProvider(generator));
                ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(generator, lookupProvider, existingFileHelper);
                generator.addProvider(event.includeClient(), blockTagsProvider);
                generator.addProvider(event.includeClient(), new ModItemTagsProvider(generator, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
                generator.addProvider(event.includeClient(), new ModLootTableProvider(generator));
                generator.addProvider(event.includeClient(), new ModEntityTypeTagsProvider(generator, lookupProvider, existingFileHelper));
            }
            if (event.includeClient()) {
                generator.addProvider(event.includeClient(), new ModBlockStateProvider(generator, existingFileHelper));
                generator.addProvider(event.includeClient(), new ModItemModelProvider(generator, existingFileHelper));
            }
        }
    }
}
