package me.desht.modularrouters.client;

import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.client.gui.filter.GuiBulkItemFilter;
import me.desht.modularrouters.client.gui.filter.GuiModFilter;
import me.desht.modularrouters.client.gui.module.*;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModContainerTypes;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderTypeLookup;

public class ClientSetup {
    public static void init() {
        setupRenderLayers();
        registerScreenFactories();
    }

    private static void setupRenderLayers() {
        RenderTypeLookup.setRenderLayer(ModBlocks.ITEM_ROUTER.get(), renderType -> true);
        RenderTypeLookup.setRenderLayer(ModBlocks.TEMPLATE_FRAME.get(), renderType -> true);
    }

    private static void registerScreenFactories() {
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_ITEM_ROUTER.get(), GuiItemRouter::new);

        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_BASIC.get(), GuiModule::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_ACTIVATOR.get(), GuiModuleActivator::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_DETECTOR.get(), GuiModuleDetector::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_DISTRIBUTOR.get(), GuiModuleDistributor::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_EXTRUDER2.get(), GuiModuleExtruder2::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_FLINGER.get(), GuiModuleFlinger::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_FLUID.get(), GuiModuleFluid::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_PLAYER.get(), GuiModulePlayer::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_VACUUM.get(), GuiModuleVacuum::new);

        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_BULK_ITEM_FILTER.get(), GuiBulkItemFilter::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MOD_FILTER.get(), GuiModFilter::new);
    }
}
