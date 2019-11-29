package me.desht.modularrouters.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class ConfigHandler {
    private static final ForgeConfigSpec.Builder S_BUILDER = new ForgeConfigSpec.Builder();
    public static final Module MODULE = new Module(S_BUILDER);
    public static final Router ROUTER = new Router(S_BUILDER);
    public static final Misc MISC = new Misc(S_BUILDER);
    public static final ForgeConfigSpec COMMON_SPEC = S_BUILDER.build();

    private static final ForgeConfigSpec.Builder C_BUILDER = new ForgeConfigSpec.Builder();
    public static final ClientMisc CLIENT_MISC = new ClientMisc(C_BUILDER);
    public static final ForgeConfigSpec CLIENT_SPEC = C_BUILDER.build();

    public static class Module {
        public final IntValue sender1BaseRange;
        public final IntValue sender1MaxRange;
        public final IntValue sender2BaseRange;
        public final IntValue sender2MaxRange;
        public final IntValue vacuumBaseRange;
        public final IntValue vacuumMaxRange;
        public final IntValue extruder1BaseRange;
        public final IntValue extruder1MaxRange;
        public final IntValue extruder2BaseRange;
        public final IntValue extruder2MaxRange;
        public final IntValue puller2BaseRange;
        public final IntValue puller2MaxRange;
        public final BooleanValue senderParticles;
        public final BooleanValue pullerParticles;
        public final BooleanValue placerParticles;
        public final BooleanValue breakerParticles;
        public final BooleanValue vacuumParticles;
        public final BooleanValue flingerEffects;
        public final BooleanValue extruderSound;
        public final BooleanValue extruderPushEntities;

        Module(ForgeConfigSpec.Builder builder) {
            builder.push("Module");

            sender1BaseRange = builder.comment("Base range for Sender Mk1 (no range upgrades)")
                    .translation("gui.config.sender1BaseRange")
                    .defineInRange("sender1BaseRange", 8, 1, Integer.MAX_VALUE);
            sender1MaxRange = builder.comment("Max range for Sender Mk1")
                    .translation("gui.config.sender1MaxRange")
                    .defineInRange("sender1MaxRange", 16, 1, Integer.MAX_VALUE);
            sender2BaseRange = builder.comment("Base range for Sender Mk2 (no range upgrades)")
                    .translation("gui.config.sender2BaseRange")
                    .defineInRange("sender2BaseRange", 24, 1, Integer.MAX_VALUE);
            sender2MaxRange = builder.comment("Max range for Sender Mk2")
                    .translation("gui.config.sender2MaxRange")
                    .defineInRange("sender2MaxRange", 48, 1, Integer.MAX_VALUE);
            vacuumBaseRange = builder.comment("Base range for Vacuum (no range upgrades)")
                    .translation("gui.config.vacuumBaseRange")
                    .defineInRange("vacuumBaseRange", 6, 1, Integer.MAX_VALUE);
            vacuumMaxRange = builder.comment("Max range for Vacuum")
                    .translation("gui.config.vacuumMaxRange")
                    .defineInRange("vacuumMaxRange", 12, 1, Integer.MAX_VALUE);
            extruder1BaseRange = builder.comment("Base range for Extruder Mk1 (no range upgrades)")
                    .translation("gui.config.extruder1BaseRange")
                    .defineInRange("extruder1BaseRange", 16, 1, Integer.MAX_VALUE);
            extruder1MaxRange = builder.comment("Max range for Extruder Mk1")
                    .translation("gui.config.extruder1MaxRange")
                    .defineInRange("extruder1MaxRange", 32, 1, Integer.MAX_VALUE);
            extruder2BaseRange = builder.comment("Base range for Extruder Mk2 (no range upgrades)")
                    .translation("gui.config.extruder2BaseRange")
                    .defineInRange("extruder2BaseRange", 32, 1, Integer.MAX_VALUE);
            extruder2MaxRange = builder.comment("Max range for Extruder Mk2")
                    .translation("gui.config.extruder2MaxRange")
                    .defineInRange("extruder2MaxRange", 64, 1, Integer.MAX_VALUE);
            puller2BaseRange = builder.comment("Base range for Puller Mk2 (no range upgrades)")
                    .translation("gui.config.puller2BaseRange")
                    .defineInRange("puller2BaseRange", 12, 1, Integer.MAX_VALUE);
            puller2MaxRange = builder.comment("Max range for Puller Mk2")
                    .translation("gui.config.puller2MaxRange")
                    .defineInRange("puller2MaxRange", 24, 1, Integer.MAX_VALUE);
            senderParticles = builder.comment("Should Sender modules show particle effects when working?")
                    .translation("gui.config.senderParticles")
                    .define("senderParticles", true);
            pullerParticles = builder.comment("Should Puller modules show particle effects when working?")
                    .translation("gui.config.pullerParticles")
                    .define("pullerParticles", true);
            placerParticles = builder.comment("Should Placer modules show particle effects when working?")
                    .translation("gui.config.placerParticles")
                    .define("placerParticles", true);
            breakerParticles = builder.comment("Should Breaker modules show particle effects when working?")
                    .translation("gui.config.breakerParticles")
                    .define("breakerParticles", true);
            vacuumParticles = builder.comment("Should Vacuum modules show particle effects when working?")
                    .translation("gui.config.vacuumParticles")
                    .define("vacuumParticles", true);
            flingerEffects = builder.comment("Should Flinger modules show smoke effects & play a sound when working?")
                    .translation("gui.config.flingerEffects")
                    .define("flingerEffects", true);
            extruderSound = builder.comment("Should Extruder Mk1/2 modules play a sound when placing/removing blocks?")
                    .translation("gui.config.extruderSound")
                    .define("extruderSound", true);
            extruderPushEntities = builder.comment("Should Extruder Mk1/2 modules push entities along when extruding blocks?")
                    .translation("gui.config.extruderPushEntities")
                    .define("extruderPushEntities", true);

            builder.pop();
        }
    }

    public static class Router {
        public final IntValue baseTickRate;
        public final IntValue ticksPerUpgrade;
        public final IntValue hardMinTickRate;
        public final IntValue ecoTimeout;
        public final IntValue lowPowerTickRate;
        public final IntValue fluidBaseTransferRate;
        public final IntValue fluidMaxTransferRate;
        public final IntValue mBperFluidUpgade;

        Router(ForgeConfigSpec.Builder builder) {
            builder.push("Router");

            baseTickRate = builder.comment("Base tick interval (in server ticks) for a router; router will run this often")
                    .translation("gui.config.baseTickRate")
                    .defineInRange("baseTickRate", 20, 1, Integer.MAX_VALUE);
            ticksPerUpgrade = builder.comment("Number of ticks by which 1 Speed Upgrade will reduce the router's tick interval")
                    .translation("gui.config.ticksPerUpgrade")
                    .defineInRange("ticksPerUpgrade", 2, 1, Integer.MAX_VALUE);
            hardMinTickRate = builder.comment("Hard minimum tick interval for a router regardless of Speed Upgrades")
                    .translation("gui.config.hardMinTickRate")
                    .defineInRange("hardMinTickRate", 2, 1, Integer.MAX_VALUE);
            ecoTimeout = builder.comment("Router with eco mode enabled will go into low-power mode if idle for this many server ticks")
                    .translation("gui.config.ecoTimeout")
                    .defineInRange("ecoTimeout", 100, 1, Integer.MAX_VALUE);
            lowPowerTickRate = builder.comment("Tick interval for an eco-mode router which has gone into low-power mode")
                    .translation("gui.config.lowPowerTickRate")
                    .defineInRange("lowPowerTickRate", 100, 1, Integer.MAX_VALUE);
            fluidBaseTransferRate = builder.comment("Base fluid transfer rate (mB/t in each direction) for a router")
                    .translation("gui.config.fluidBaseTransferRate")
                    .defineInRange("fluidBaseTransferRate", 50, 1, Integer.MAX_VALUE);
            fluidMaxTransferRate = builder.comment("Max fluid transfer rate (mB/t in each direction) for a router")
                    .translation("gui.config.baseTickRate")
                    .defineInRange("fluidMaxTransferRate", 400, 1, Integer.MAX_VALUE);
            mBperFluidUpgade = builder.comment("Fluid transfer rate increase per Fluid Transfer Upgrade")
                    .translation("gui.config.mBperFluidUpgade")
                    .defineInRange("mBperFluidUpgade", 10, 1, Integer.MAX_VALUE);


            builder.pop();
        }
    }

    public static class Misc {
        public final BooleanValue startWithGuide;

        Misc(ForgeConfigSpec.Builder builder) {
            builder.push("Misc");

            startWithGuide = builder.comment("Should new players get a Modular Routers guidebook?")
                    .translation("gui.config.startWithGuide")
                    .define("startWithGuide", true);

            builder.pop();
        }
    }

    public static class ClientMisc {
        public final BooleanValue alwaysShowModuleSettings;
        public final BooleanValue moduleGuiBackgroundTint;

        ClientMisc(ForgeConfigSpec.Builder builder) {
            builder.push("Misc");

            alwaysShowModuleSettings = builder.comment("Should module tooltips always show module settings (without needing to hold Shift)?")
                    .translation("gui.config.alwaysShowSettings")
                    .define("alwaysShowSettings", true);
            moduleGuiBackgroundTint = builder.comment("Should module GUI's be tinted according to the module item colour?")
                    .translation("gui.config.moduleGuiBackgroundTint")
                    .define("moduleGuiBackgroundTint", true);

            builder.pop();

        }
    }
}
