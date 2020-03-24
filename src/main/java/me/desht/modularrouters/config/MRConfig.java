package me.desht.modularrouters.config;

public class MRConfig {
    public static class Client {
        public static class Misc {
            public static boolean alwaysShowModuleSettings;
            public static boolean moduleGuiBackgroundTint;
        }
    }

    public static class Common {
        public static class Module {
            public static int sender1BaseRange;
            public static int sender1MaxRange;
            public static int sender2BaseRange;
            public static int sender2MaxRange;
            public static int vacuumBaseRange;
            public static int vacuumMaxRange;
            public static int extruder1BaseRange;
            public static int extruder1MaxRange;
            public static int extruder2BaseRange;
            public static int extruder2MaxRange;
            public static int puller2BaseRange;
            public static int puller2MaxRange;
            public static int fluid2BaseRange;
            public static int fluid2MaxRange;
            public static boolean senderParticles;
            public static boolean pullerParticles;
            public static boolean placerParticles;
            public static boolean breakerParticles;
            public static boolean vacuumParticles;
            public static boolean flingerEffects;
            public static boolean extruderSound;
            public static boolean extruderPushEntities;
        }

        public static class Router {
            public static int baseTickRate;
            public static int ticksPerUpgrade;
            public static int hardMinTickRate;
            public static int ecoTimeout;
            public static int lowPowerTickRate;
            public static int fluidBaseTransferRate;
            public static int fluidMaxTransferRate;
            public static int mBperFluidUpgade;
        }
    }
}
