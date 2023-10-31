package me.desht.modularrouters.integration;

import me.desht.modularrouters.integration.top.TOPCompatibility;
import net.neoforged.fml.ModList;

public class IntegrationHandler {
    public static void registerAll() {
        registerTOP();

        // JEI and HWYLA registration are implicit; annotation-driven
    }

    private static void registerTOP() {
        if (ModList.get().isLoaded("theoneprobe")) {
            TOPCompatibility.register();
        }
    }
}
