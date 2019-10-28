package me.desht.modularrouters.client.gui;

import me.desht.modularrouters.client.gui.filter.GuiBulkItemFilter;
import me.desht.modularrouters.client.gui.filter.GuiModFilter;
import me.desht.modularrouters.client.gui.module.*;
import me.desht.modularrouters.core.ModContainerTypes;
import net.minecraft.client.gui.ScreenManager;

public class ScreenFactoryRegistration {
    public static void registerScreenFactories() {
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_ITEM_ROUTER, GuiItemRouter::new);

        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_BASIC, GuiModule::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_ACTIVATOR, GuiModuleActivator::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_DETECTOR, GuiModuleDetector::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_DISTRIBUTOR, GuiModuleDistributor::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_EXTRUDER2, GuiModuleExtruder2::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_FLINGER, GuiModuleFlinger::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_FLUID, GuiModuleFluid::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_PLAYER, GuiModulePlayer::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_VACUUM, GuiModuleVacuum::new);

        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_BULK_ITEM_FILTER, GuiBulkItemFilter::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MOD_FILTER, GuiModFilter::new);
    }
}
