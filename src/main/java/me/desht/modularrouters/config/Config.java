package me.desht.modularrouters.config;

import me.desht.modularrouters.ModularRouters;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Config {
    public static final int DEFAULT_BASE_TICK_RATE = 20;
    private static final int DEFAULT_TICKS_PER_UPGRADE = 2;
    private static final boolean DEFAULT_SENDER_PARTICLES = true;
    private static final int DEFAULT_SENDER1_BASE_RANGE = 10;
    public static final int MAX_SENDER1_RANGE = DEFAULT_SENDER1_BASE_RANGE * 2;
    private static final int DEFAULT_SENDER2_BASE_RANGE = 25;
    public static final int MAX_SENDER2_RANGE = DEFAULT_SENDER2_BASE_RANGE * 2;
    private static final boolean DEFAULT_VACUUM_PARTICLES = true;
    private static final int DEFAULT_VACUUM_BASE_RANGE = 6;
    public static final int MAX_VACUUM_RANGE = DEFAULT_VACUUM_BASE_RANGE * 2;
    private static final boolean DEFAULT_PLACER_PARTICLES = true;
    private static final boolean DEFAULT_BREAKER_PARTICLES = true;

    public static int baseTickRate;
    public static int ticksPerUpgrade;
    public static int hardMinTickRate;
    public static boolean vacuumParticles;
    public static boolean senderParticles;
    public static int sender1BaseRange;
    public static int sender2BaseRange;
    public static int vacuumBaseRange;
    public static boolean breakerParticles;
    public static boolean placerParticles;

    public static final String CATEGORY_NAME_ROUTER = "category_router";
    public static final String CATEGORY_NAME_MODULE = "category_module";
    private static Configuration config;

    public static Configuration getConfig() {
        return config;
    }

    public static void preInit() {
        File configFile = new File(Loader.instance().getConfigDir(), "modularrouters.cfg");
        config = new Configuration(configFile);
        syncFromFile();
    }

    public static void clientPreInit() {
        //register the save config handler to the forge mod loader event bus
        // creates an instance of the static class ConfigEventHandler and has it listen
        // on the FML bus (see Notes and ConfigEventHandler for more information)
        MinecraftForge.EVENT_BUS.register(new ConfigEventHandler());
    }

    public static void syncFromFile() {
        syncConfig(true, true);
    }

    public static void syncFromGUI() {
        syncConfig(false, true);
    }

    public static void syncFromFields() {
        syncConfig(false, false);
    }

    private static void syncConfig(boolean loadConfigFromFile, boolean readFieldsFromConfig) {
        System.out.println("syncConfig(" + loadConfigFromFile + "," + readFieldsFromConfig + ")");
        if (loadConfigFromFile) {
            config.load();
        }

        Property propBaseTickRate = config.get(CATEGORY_NAME_ROUTER, "baseTickRate", DEFAULT_BASE_TICK_RATE,
                "Base router tick rate", 1, Integer.MAX_VALUE);
        propBaseTickRate.setLanguageKey("gui.config.baseTickRate");
        Property propTicksPerUpgrade = config.get(CATEGORY_NAME_ROUTER, "ticksPerUpgrade", DEFAULT_TICKS_PER_UPGRADE,
                "Tick rate reduction per upgrade installed", 2, 10);
        propTicksPerUpgrade.setLanguageKey("gui.config.ticksPerUpgrade");
        Property propHardMinTicks = config.get(CATEGORY_NAME_ROUTER, "hardMinTicks", 2,
                "Hard minimum tick rate", 1, Integer.MAX_VALUE);
        propHardMinTicks.setLanguageKey("gui.config.hardMinTicks");

        Property propSenderParticles = config.get(CATEGORY_NAME_MODULE, "senderParticles", DEFAULT_SENDER_PARTICLES,
                "Show particles when Sender Modules send items");
        propSenderParticles.setLanguageKey("gui.config.senderParticles");
        Property propSender1BaseRange = config.get(CATEGORY_NAME_MODULE, "sender1BaseRange", DEFAULT_SENDER1_BASE_RANGE,
                "Sender Module Mk1 Base Range", 1, MAX_SENDER1_RANGE);
        propSender1BaseRange.setLanguageKey("gui.config.sender1BaseRange");
        Property propSender2BaseRange = config.get(CATEGORY_NAME_MODULE, "sender2BaseRange", DEFAULT_SENDER2_BASE_RANGE,
                "Sender Module Mk2 Base Range", 1, MAX_SENDER2_RANGE);
        propSender2BaseRange.setLanguageKey("gui.config.sender2BaseRange");
        Property propVacuumParticles = config.get(CATEGORY_NAME_MODULE, "vacuumParticles", DEFAULT_VACUUM_PARTICLES,
                "Show particles when Vacuum Module absorbs items");
        propVacuumParticles.setLanguageKey("gui.config.vacuumParticles");
        Property propVacuumBaseRange = config.get(CATEGORY_NAME_MODULE, "vacuumBaseRange", DEFAULT_VACUUM_BASE_RANGE,
                "Vacuum Module Base Range", DEFAULT_VACUUM_BASE_RANGE, DEFAULT_VACUUM_BASE_RANGE * 2);
        propVacuumBaseRange.setLanguageKey("gui.config.vacuumBaseRange");
        Property propPlacerParticles = config.get(CATEGORY_NAME_MODULE, "placerParticles", DEFAULT_PLACER_PARTICLES,
                "Show particles when Placer Module places a block");
        propPlacerParticles.setLanguageKey("gui.config.placerParticles");
        Property propBreakerParticles = config.get(CATEGORY_NAME_MODULE, "breakerParticles", DEFAULT_BREAKER_PARTICLES,
                "Show particles when Breaker Module breaks a block");
        propBreakerParticles.setLanguageKey("gui.config.breakerParticles");

        List<String> propOrderGeneral = new ArrayList<>();
        propOrderGeneral.add(propBaseTickRate.getName());
        propOrderGeneral.add(propTicksPerUpgrade.getName());
        propOrderGeneral.add(propHardMinTicks.getName());
        List<String> propOrderModule = new ArrayList<>();
        propOrderModule.add(propSender1BaseRange.getName());
        propOrderModule.add(propSender2BaseRange.getName());
        propOrderModule.add(propVacuumBaseRange.getName());
        propOrderModule.add(propSenderParticles.getName());
        propOrderModule.add(propVacuumParticles.getName());
        propOrderModule.add(propPlacerParticles.getName());
        propOrderModule.add(propBreakerParticles.getName());
        config.setCategoryPropertyOrder(CATEGORY_NAME_ROUTER, propOrderGeneral);
        config.setCategoryPropertyOrder(CATEGORY_NAME_MODULE, propOrderModule);

        if (readFieldsFromConfig) {
            baseTickRate = Math.max(1, propBaseTickRate.getInt(DEFAULT_BASE_TICK_RATE));
            ticksPerUpgrade = propTicksPerUpgrade.getInt(DEFAULT_TICKS_PER_UPGRADE);
            hardMinTickRate = propHardMinTicks.getInt();
            senderParticles = propSenderParticles.getBoolean();
            sender1BaseRange = propSender1BaseRange.getInt();
            sender2BaseRange = propSender2BaseRange.getInt();
            vacuumParticles = propVacuumParticles.getBoolean();
            vacuumBaseRange = propVacuumBaseRange.getInt();
            placerParticles = propPlacerParticles.getBoolean();
            breakerParticles = propBreakerParticles.getBoolean();
        }

        propBaseTickRate.set(baseTickRate);
        propTicksPerUpgrade.set(ticksPerUpgrade);
        propHardMinTicks.set(hardMinTickRate);
        propSenderParticles.set(senderParticles);
        propSender1BaseRange.set(sender1BaseRange);
        propSender2BaseRange.set(sender2BaseRange);
        propVacuumParticles.set(vacuumParticles);
        propVacuumBaseRange.set(vacuumBaseRange);
        propPlacerParticles.set(placerParticles);
        propBreakerParticles.set(breakerParticles);

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static class ConfigEventHandler {
        @SubscribeEvent(priority = EventPriority.NORMAL)
        public void onEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (ModularRouters.modId.equals(event.getModID()) && !event.isWorldRunning()) {
                if (event.getConfigID().equals(CATEGORY_NAME_ROUTER) || event.getConfigID().equals(CATEGORY_NAME_MODULE)) {
                    syncFromGUI();
                }
            }
        }
    }
}
