package me.desht.modularrouters.config;

import java.util.Set;

public class MRConfig {
    public static class Client {
        public static class Misc {
            public static boolean alwaysShowModuleSettings;
            public static boolean moduleGuiBackgroundTint;
            public static boolean renderFlyingItems;
            public static boolean heldRouterShowsCamoRouters;
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
            public static boolean breakerHarvestLevelLimit;
            public static Set<String> activatorEntityBlacklist;
            public static Set<String> activatorEntityAttackBlacklist;
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
            public static int fePerEnergyUpgrade;
            public static int feXferPerEnergyUpgrade;
            public static boolean blockBreakXPDrops;
        }

        public static class EnergyCosts {
            public static int activatorModuleEnergyCost;
            public static int activatorModuleEnergyCostAttack;
            public static int breakerModuleEnergyCost;
            public static int detectorModuleEnergyCost;
            public static int distributorModuleEnergyCost;
            public static int dropperModuleEnergyCost;
            public static int energydistributorModuleEnergyCost;
            public static int energyoutputModuleEnergyCost;
            public static int extruderModule1EnergyCost;
            public static int extruderModule2EnergyCost;
            public static int flingerModuleEnergyCost;
            public static int fluidModuleEnergyCost;
            public static int fluidModule2EnergyCost;
            public static int placerModuleEnergyCost;
            public static int playerModuleEnergyCost;
            public static int pullerModule1EnergyCost;
            public static int pullerModule2EnergyCost;
            public static int senderModule1EnergyCost;
            public static int senderModule2EnergyCost;
            public static int senderModule3EnergyCost;
            public static int vacuumModuleEnergyCost;
            public static int voidModuleEnergyCost;
        }
    }
}
