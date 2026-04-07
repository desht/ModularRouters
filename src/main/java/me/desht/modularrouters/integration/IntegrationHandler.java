package me.desht.modularrouters.integration;

import me.desht.modularrouters.integration.ffs.FFSSetup;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class IntegrationHandler {
    public static void onModConstruction(IEventBus modBus) {
        registerFFS(modBus);
    }

    public static void onCommonSetup() {
//        registerTOP();

        // JEI and HWYLA registration are implicit; annotation-driven
    }

    private static void registerFFS(IEventBus modBus) {
        if (ModList.get().isLoaded("ftbfiltersystem")) {
            modBus.addListener(FFSSetup::registerCaps);
        }
    }
//
//    private static void registerTOP() {
//        if (ModList.get().isLoaded("theoneprobe")) {
//            TOPCompatibility.register();
//        }
//    }
}
