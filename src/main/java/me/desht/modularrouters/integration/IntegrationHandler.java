package me.desht.modularrouters.integration;

import me.desht.modularrouters.integration.tesla.TeslaIntegration;
import me.desht.modularrouters.integration.top.TOPCompatibility;
import net.minecraftforge.fml.common.Loader;

public class IntegrationHandler {
    public static void registerTOP() {
        if (Loader.isModLoaded("theoneprobe")) {
            TOPCompatibility.register();
        }
    }

//    public static void registerWaila() {
//        if (Loader.isModLoaded("Waila")) {
//            WailaIntegration.setup();
//        }
//    }

//    public static void registerGuideBook() {
//        if (Loader.isModLoaded("guideapi")) {
//            Guidebook.buildGuide();
//            ModularRouters.proxy.addGuidebookModel(Guidebook.guideBook);
//        }
//    }

    public static void registerTesla() {
        if (Loader.isModLoaded("tesla")) {
            TeslaIntegration.setup();
        }
    }
}
