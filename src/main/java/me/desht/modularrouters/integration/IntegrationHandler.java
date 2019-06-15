package me.desht.modularrouters.integration;

import me.desht.modularrouters.integration.top.TOPCompatibility;
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
            TOPCompatibility.register();
        }
    }

    public static void registerWaila() {
        if (ModList.get().isLoaded("waila")) {
            // todo 1.14
//            WailaIntegration.setup();
        }
    }

    public static void checkForXpJuice() {
        // todo 1.14
//        fluidXpJuice = FluidRegistry.getFluid("xpjuice");
    }
}
