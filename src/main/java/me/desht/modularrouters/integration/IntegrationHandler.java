package me.desht.modularrouters.integration;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.integration.guideapi.Guidebook;
import me.desht.modularrouters.integration.tesla.TeslaIntegration;
import me.desht.modularrouters.integration.top.TOPCompatibility;
import me.desht.modularrouters.integration.waila.WailaIntegration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;

public class IntegrationHandler {
    public static Fluid fluidXpJuice = null;

    public static void registerTOP() {
        if (Loader.isModLoaded("theoneprobe")) {
            TOPCompatibility.register();
        }
    }

    public static void registerWaila() {
        if (Loader.isModLoaded("Waila")) {
            WailaIntegration.setup();
        }
    }

    public static void registerGuideBook() {
        if (Loader.isModLoaded("guideapi")) {
            Guidebook.buildGuide();
            ModularRouters.proxy.addGuidebookModel(Guidebook.guideBook);
        }
    }

    public static void registerTesla() {
        if (Loader.isModLoaded("tesla")) {
            TeslaIntegration.setup();
        }
    }

    public static void checkForXpJuice() {
        fluidXpJuice = FluidRegistry.getFluid("xpjuice");
    }
}
