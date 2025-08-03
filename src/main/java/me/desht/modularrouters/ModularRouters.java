package me.desht.modularrouters;

import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.*;
import me.desht.modularrouters.datagen.*;
import me.desht.modularrouters.integration.IntegrationHandler;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.util.ModNameCache;
import me.desht.modularrouters.util.WildcardedRLMatcher;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ModularRouters.MODID)
public class ModularRouters {
    public static final String MODID = "modularrouters";
    public static final String MODNAME = "Modular Routers";

    public static final Logger LOGGER = LogManager.getLogger(MODNAME);

    private static WildcardedRLMatcher dimensionBlacklist;

    public ModularRouters(ModContainer container, IEventBus modBus) {
        ConfigHolder.init(container, modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerCaps);

        IntegrationHandler.onModConstruction(modBus);

        registerDeferred(modBus);
    }

    private static void registerDeferred(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenuTypes.MENUS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModRecipes.RECIPES.register(modBus);
        ModCreativeModeTabs.TABS.register(modBus);
        ModDataComponents.COMPONENTS.register(modBus);
    }

    private void registerCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MODULAR_ROUTER.get(),
                (be, side) -> be.getBuffer());

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MODULAR_ROUTER.get(),
                (be, side) -> be.getFluidHandler());

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.MODULAR_ROUTER.get(),
                (be, side) -> be.getEnergyStorage());
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

        event.enqueueWork(() -> {
            IntegrationHandler.onCommonSetup();
            XPCollection.detectXPTypes();
            ModNameCache.init();
        });
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
    public static class DataGenerators {
        @SubscribeEvent
        public static void gatherData(GatherDataEvent.Client event) {
            event.createProvider(ModRecipeProvider.Runner::new);
            event.createBlockAndItemTags(ModBlockTagsProvider::new, ModItemTagsProvider::new);
            event.createProvider(ModLootTableProvider::new);
            event.createProvider(ModEntityTypeTagsProvider::new);
            event.createProvider(ModModelProvider::new);
        }
    }
}
