package me.desht.modularrouters.integration;

import me.desht.modularrouters.integration.top.TOPCompatibility;
import net.minecraftforge.fml.ModList;

public class IntegrationHandler {

    public static void registerAll() {
        IntegrationHandler.registerWaila();
        IntegrationHandler.registerTOP();
    }

    private static void registerTOP() {
        if (ModList.get().isLoaded("theoneprobe")) {
//            TOPCompatibility.register();
        }
    }

    public static void registerWaila() {
        // nothing specific to do; registration is implicit
    }

}
