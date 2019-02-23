package me.desht.modularrouters.integration;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.ModList;

public class IntegrationHandler {
    public static Fluid fluidXpJuice = null;

    public static void registerAll() {
        IntegrationHandler.registerWaila();
        IntegrationHandler.registerTOP();
    }

    public static void registerTOP() {
        if (ModList.get().isLoaded("theoneprobe")) {
            // todo 1.13
//            TOPCompatibility.register();
        }
    }

    public static void registerWaila() {
        if (ModList.get().isLoaded("waila")) {
            // todo 1.13
//            WailaIntegration.setup();
        }
    }

    public static void checkForXpJuice() {
        // todo 1.13
//        fluidXpJuice = FluidRegistry.getFluid("xpjuice");
    }
}
