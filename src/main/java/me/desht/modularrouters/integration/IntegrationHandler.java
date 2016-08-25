package me.desht.modularrouters.integration;

import me.desht.modularrouters.integration.top.TOPCompatibility;
import me.desht.modularrouters.integration.waila.WailaIntegration;
import net.minecraftforge.fml.common.Loader;

public class IntegrationHandler {
    public static void registerTOP() {
        if (Loader.isModLoaded("theoneprobe")) {
            TOPCompatibility.register();
        }
    }

    public static void registerWaila() {
        WailaIntegration.setup();
    }
}
