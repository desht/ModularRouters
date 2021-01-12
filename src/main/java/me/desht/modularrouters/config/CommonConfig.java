package me.desht.modularrouters.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

import java.util.List;

public class CommonConfig {
    public class Module {
        IntValue sender1BaseRange;
        IntValue sender1MaxRange;
        IntValue sender2BaseRange;
        IntValue sender2MaxRange;
        IntValue vacuumBaseRange;
        IntValue vacuumMaxRange;
        IntValue extruder1BaseRange;
        IntValue extruder1MaxRange;
        IntValue extruder2BaseRange;
        IntValue extruder2MaxRange;
        IntValue puller2BaseRange;
        IntValue puller2MaxRange;
        IntValue fluid2BaseRange;
        IntValue fluid2MaxRange;
        BooleanValue senderParticles;
        BooleanValue pullerParticles;
        BooleanValue placerParticles;
        BooleanValue breakerParticles;
        BooleanValue vacuumParticles;
        BooleanValue flingerEffects;
        BooleanValue extruderSound;
        BooleanValue extruderPushEntities;
        BooleanValue breakerHarvestLevelLimit;
        ForgeConfigSpec.ConfigValue<List<String>> activatorEntityBlacklist;
    }

    public static class Router {
        IntValue baseTickRate;
        IntValue ticksPerUpgrade;
        IntValue hardMinTickRate;
        IntValue ecoTimeout;
        IntValue lowPowerTickRate;
        IntValue fluidBaseTransferRate;
        IntValue fluidMaxTransferRate;
        IntValue mBperFluidUpgade;
    }

    public final Module module = new Module();
    public final Router router = new Router();

    CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Module");
        module.sender1BaseRange = builder.comment("Base range for Sender Mk1 (no range upgrades)")
                .translation("modularrouters.gui.config.sender1BaseRange")
                .defineInRange("sender1BaseRange", 8, 1, Integer.MAX_VALUE);
        module.sender1MaxRange = builder.comment("Max range for Sender Mk1")
                .translation("modularrouters.gui.config.sender1MaxRange")
                .defineInRange("sender1MaxRange", 16, 1, Integer.MAX_VALUE);
        module.sender2BaseRange = builder.comment("Base range for Sender Mk2 (no range upgrades)")
                .translation("modularrouters.gui.config.sender2BaseRange")
                .defineInRange("sender2BaseRange", 24, 1, Integer.MAX_VALUE);
        module.sender2MaxRange = builder.comment("Max range for Sender Mk2")
                .translation("modularrouters.gui.config.sender2MaxRange")
                .defineInRange("sender2MaxRange", 48, 1, Integer.MAX_VALUE);
        module.vacuumBaseRange = builder.comment("Base range for Vacuum (no range upgrades)")
                .translation("modularrouters.gui.config.vacuumBaseRange")
                .defineInRange("vacuumBaseRange", 6, 1, Integer.MAX_VALUE);
        module.vacuumMaxRange = builder.comment("Max range for Vacuum")
                .translation("modularrouters.gui.config.vacuumMaxRange")
                .defineInRange("vacuumMaxRange", 12, 1, Integer.MAX_VALUE);
        module.extruder1BaseRange = builder.comment("Base range for Extruder Mk1 (no range upgrades)")
                .translation("gui.config.extruder1BaseRange")
                .defineInRange("extruder1BaseRange", 16, 1, Integer.MAX_VALUE);
        module.extruder1MaxRange = builder.comment("Max range for Extruder Mk1")
                .translation("gui.config.extruder1MaxRange")
                .defineInRange("extruder1MaxRange", 32, 1, Integer.MAX_VALUE);
        module.extruder2BaseRange = builder.comment("Base range for Extruder Mk2 (no range upgrades)")
                .translation("modularrouters.gui.config.extruder2BaseRange")
                .defineInRange("extruder2BaseRange", 32, 1, Integer.MAX_VALUE);
        module.extruder2MaxRange = builder.comment("Max range for Extruder Mk2")
                .translation("modularrouters.gui.config.extruder2MaxRange")
                .defineInRange("extruder2MaxRange", 64, 1, Integer.MAX_VALUE);
        module.puller2BaseRange = builder.comment("Base range for Puller Mk2 (no range upgrades)")
                .translation("modularrouters.gui.config.puller2BaseRange")
                .defineInRange("puller2BaseRange", 12, 1, Integer.MAX_VALUE);
        module.puller2MaxRange = builder.comment("Max range for Puller Mk2")
                .translation("modularrouters.gui.config.puller2MaxRange")
                .defineInRange("puller2MaxRange", 24, 1, Integer.MAX_VALUE);
        module.fluid2BaseRange = builder.comment("Base range for Fluid Mk2 (no range upgrades)")
                .translation("gui.config.fluid2BaseRange")
                .defineInRange("fluid2BaseRange", 12, 1, Integer.MAX_VALUE);
        module.fluid2MaxRange = builder.comment("Max range for Fluid Mk2")
                .translation("gui.config.fluid2MaxRange")
                .defineInRange("fluid2MaxRange", 24, 1, Integer.MAX_VALUE);
        module.senderParticles = builder.comment("Should Sender modules show particle effects when working?")
                .translation("modularrouters.gui.config.senderParticles")
                .define("senderParticles", true);
        module.pullerParticles = builder.comment("Should Puller modules show particle effects when working?")
                .translation("modularrouters.gui.config.pullerParticles")
                .define("pullerParticles", true);
        module.placerParticles = builder.comment("Should Placer modules show particle effects when working?")
                .translation("modularrouters.gui.config.placerParticles")
                .define("placerParticles", true);
        module.breakerParticles = builder.comment("Should Breaker modules show particle effects when working?")
                .translation("modularrouters.gui.config.breakerParticles")
                .define("breakerParticles", true);
        module.vacuumParticles = builder.comment("Should Vacuum modules show particle effects when working?")
                .translation("modularrouters.gui.config.vacuumParticles")
                .define("vacuumParticles", true);
        module.flingerEffects = builder.comment("Should Flinger modules show smoke effects & play a sound when working?")
                .translation("modularrouters.gui.config.flingerEffects")
                .define("flingerEffects", true);
        module.extruderSound = builder.comment("Should Extruder Mk1/2 modules play a sound when placing/removing blocks?")
                .translation("modularrouters.gui.config.extruderSound")
                .define("extruderSound", true);
        module.extruderPushEntities = builder.comment("Should Extruder Mk1/2 modules push entities along when extruding blocks?")
                .translation("modularrouters.gui.config.extruderPushEntities")
                .define("extruderPushEntities", true);
        module.breakerHarvestLevelLimit = builder.comment("Should Breaker & Extruder Mk1 Modules respect the harvest level of the pickaxe used to craft them? (e.g. craft with an Iron Pickaxe => can't break Obsidian")
                .translation("gui.config.breakerHarvestLevelLimit")
                .define("breakerHarvestLevelLimit", true);
        module.activatorEntityBlacklist = builder.comment("List of entity types which the Activator Module (in entity mode) should never attempt to interact with. Any entity which opens a GUI when right-clicked, like Villagers, should be added here, especially if it prevents player interaction.")
                .translation("gui.config.activatorEntityBlacklist")
                .define("activatorEntityBlacklist", Lists.newArrayList("minecraft:villager", "minecraft:wandering_trader"));
        builder.pop();

        builder.push("Router");
        router.baseTickRate = builder.comment("Base tick interval (in server ticks) for a router; router will run this often")
                .translation("modularrouters.gui.config.baseTickRate")
                .defineInRange("baseTickRate", 20, 1, Integer.MAX_VALUE);
        router.ticksPerUpgrade = builder.comment("Number of ticks by which 1 Speed Upgrade will reduce the router's tick interval")
                .translation("modularrouters.gui.config.ticksPerUpgrade")
                .defineInRange("ticksPerUpgrade", 2, 1, Integer.MAX_VALUE);
        router.hardMinTickRate = builder.comment("Hard minimum tick interval for a router regardless of Speed Upgrades")
                .translation("gui.config.hardMinTickRate")
                .defineInRange("hardMinTickRate", 2, 1, Integer.MAX_VALUE);
        router.ecoTimeout = builder.comment("Router with eco mode enabled will go into low-power mode if idle for this many server ticks")
                .translation("modularrouters.gui.config.ecoTimeout")
                .defineInRange("ecoTimeout", 100, 1, Integer.MAX_VALUE);
        router.lowPowerTickRate = builder.comment("Tick interval for an eco-mode router which has gone into low-power mode")
                .translation("modularrouters.gui.config.lowPowerTickRate")
                .defineInRange("lowPowerTickRate", 100, 1, Integer.MAX_VALUE);
        router.fluidBaseTransferRate = builder.comment("Base fluid transfer rate (mB/t in each direction) for a router")
                .translation("gui.config.fluidBaseTransferRate")
                .defineInRange("fluidBaseTransferRate", 50, 1, Integer.MAX_VALUE);
        router.fluidMaxTransferRate = builder.comment("Max fluid transfer rate (mB/t in each direction) for a router")
                .translation("modularrouters.gui.config.baseTickRate")
                .defineInRange("fluidMaxTransferRate", 400, 1, Integer.MAX_VALUE);
        router.mBperFluidUpgade = builder.comment("Fluid transfer rate increase per Fluid Transfer Upgrade")
                .translation("gui.config.mBperFluidUpgade")
                .defineInRange("mBperFluidUpgade", 10, 1, Integer.MAX_VALUE);
        builder.pop();
    }
}
