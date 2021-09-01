package me.desht.modularrouters.integration;

import me.desht.modularrouters.integration.top.TOPCompatibility;
import net.minecraftforge.fml.ModList;

public class IntegrationHandler {

    public static void registerAll() {
        IntegrationHandler.registerTOP();

        // TOP and HWYLA registration are implicit; annotation-driven
    }

    private static void registerTOP() {
        if (ModList.get().isLoaded("theoneprobe")) {
            TOPCompatibility.register();
        }
    }
}
