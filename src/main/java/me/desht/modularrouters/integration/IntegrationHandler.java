package me.desht.modularrouters.integration;

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
        if (Loader.isModLoaded("waila")) {
            WailaIntegration.setup();
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
