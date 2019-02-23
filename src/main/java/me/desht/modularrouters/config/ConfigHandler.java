package me.desht.modularrouters.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHandler {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final Module MODULE = new Module(BUILDER);
    public static final Router ROUTER = new Router(BUILDER);
    public static final Misc MISC = new Misc(BUILDER);
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static class Module {
        public final ForgeConfigSpec.ConfigValue<Integer> sender1BaseRange;
        public final ForgeConfigSpec.ConfigValue<Integer> sender1MaxRange;
        public final ForgeConfigSpec.ConfigValue<Integer> sender2BaseRange;
        public final ForgeConfigSpec.ConfigValue<Integer> sender2MaxRange;
        public final ForgeConfigSpec.ConfigValue<Integer> vacuumBaseRange;
        public final ForgeConfigSpec.ConfigValue<Integer> vacuumMaxRange;
        public final ForgeConfigSpec.ConfigValue<Integer> extruder1BaseRange;
        public final ForgeConfigSpec.ConfigValue<Integer> extruder1MaxRange;
        public final ForgeConfigSpec.ConfigValue<Integer> extruder2BaseRange;
        public final ForgeConfigSpec.ConfigValue<Integer> extruder2MaxRange;
        public final ForgeConfigSpec.ConfigValue<Integer> puller2BaseRange;
        public final ForgeConfigSpec.ConfigValue<Integer> puller2MaxRange;
        public final ForgeConfigSpec.ConfigValue<Boolean> senderParticles;
        public final ForgeConfigSpec.ConfigValue<Boolean> pullerParticles;
        public final ForgeConfigSpec.ConfigValue<Boolean> placerParticles;
        public final ForgeConfigSpec.ConfigValue<Boolean> breakerParticles;
        public final ForgeConfigSpec.ConfigValue<Boolean> vacuumParticles;
        public final ForgeConfigSpec.ConfigValue<Boolean> flingerEffects;
        public final ForgeConfigSpec.ConfigValue<Boolean> extruderSound;
        public final ForgeConfigSpec.ConfigValue<Boolean> guiBackgroundTint;
        public final ForgeConfigSpec.ConfigValue<Boolean> extruderPushEntities;

        public Module(ForgeConfigSpec.Builder builder) {
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
            guiBackgroundTint = builder.comment("Should module GUI's be tinted according to the module item colour?")
                    .translation("gui.config.guiBackgroundTint")
                    .define("guiBackgroundTint", true);

            builder.pop();
        }
    }

    public static class Router {
        public final ForgeConfigSpec.ConfigValue<Integer> baseTickRate;
        public final ForgeConfigSpec.ConfigValue<Integer> ticksPerUpgrade;
        public final ForgeConfigSpec.ConfigValue<Integer> hardMinTickRate;
        public final ForgeConfigSpec.ConfigValue<Integer> ecoTimeout;
        public final ForgeConfigSpec.ConfigValue<Integer> lowPowerTickRate;
        public final ForgeConfigSpec.ConfigValue<Integer> fluidBaseTransferRate;
        public final ForgeConfigSpec.ConfigValue<Integer> fluidMaxTransferRate;
        public final ForgeConfigSpec.ConfigValue<Integer> mBperFluidUpgade;

        public Router(ForgeConfigSpec.Builder builder) {
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
        public ForgeConfigSpec.ConfigValue<Boolean> startWithGuide;
        public ForgeConfigSpec.ConfigValue<Boolean> alwaysShowSettings;

        public Misc(ForgeConfigSpec.Builder builder) {
            builder.push("Misc");

            startWithGuide = builder.comment("Should new players get a Modular Routers guidebook?")
                    .translation("gui.config.startWithGuide")
                    .define("startWithGuide", true);
            alwaysShowSettings = builder.comment("Should module tooltips always show module settings (without needing to hold Shift)?")
                    .translation("gui.config.alwaysShowSettings")
                    .define("alwaysShowSettings", true);

            builder.pop();
        }
    }
}
