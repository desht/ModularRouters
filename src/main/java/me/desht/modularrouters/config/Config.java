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
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Config {

    public static class Defaults {

        public static final int BASE_TICK_RATE = 20;
        public static final int TICKS_PER_UPGRADE = 2;
        public static final int SENDER1_BASE_RANGE = 8;
        public static final int SENDER1_MAX_RANGE = SENDER1_BASE_RANGE * 2;
        public static final int SENDER2_BASE_RANGE = 16;
        public static final int SENDER2_MAX_RANGE = SENDER2_BASE_RANGE * 2;
        public static final int VACUUM_BASE_RANGE = 6;
        public static final int VACUUM_MAX_RANGE = VACUUM_BASE_RANGE * 2;
        public static final boolean SENDER_PARTICLES = true;
        public static final boolean VACUUM_PARTICLES = true;
        public static final boolean PLACER_PARTICLES = true;
        public static final boolean BREAKER_PARTICLES = true;
        public static final boolean EXTRUDER_SOUND = true;
        public static final char CONFIG_KEY = 'c';
        public static final boolean START_WITH_GUIDE = false;
        public static final int ECO_TIMEOUT = 300;
        public static final int LOW_POWER_INTERVAL = 100;
    }

    public static char configKey;
    public static int baseTickRate;
    public static int ticksPerUpgrade;
    public static int hardMinTickRate;
    public static int ecoTimeout;
    public static int lowPowerTickRate;
    public static int sender1BaseRange;
    public static int sender1MaxRange;
    public static int sender2BaseRange;
    public static int sender2MaxRange;
    public static int vacuumBaseRange;
    public static int vacuumMaxRange;
    public static boolean senderParticles;
    public static boolean breakerParticles;
    public static boolean extruderSound;
    public static boolean placerParticles;
    public static boolean vacuumParticles;
    public static boolean startWithGuide;

    public static final String CATEGORY_NAME_ROUTER = "category_router";
    public static final String CATEGORY_NAME_MODULE = "category_module";
    public static final String CATEGORY_NAME_MISC = "category_misc";
    private static Configuration config;

    public static Configuration getConfig() {
        return config;
    }

    private static Pattern oneCharPattern = Pattern.compile("^.$");

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
        if (loadConfigFromFile) {
            config.load();
        }

        Property propConfigKey = config.get(CATEGORY_NAME_ROUTER, "configKey", String.valueOf(Defaults.CONFIG_KEY),
                "Keypress to configure installed modules in-place", oneCharPattern);
        propConfigKey.setLanguageKey("gui.config.configKey");
        Property propBaseTickRate = config.get(CATEGORY_NAME_ROUTER, "baseTickRate", Defaults.BASE_TICK_RATE,
                "Base router tick rate", 1, Integer.MAX_VALUE);
        propBaseTickRate.setLanguageKey("gui.config.baseTickRate");
        Property propTicksPerUpgrade = config.get(CATEGORY_NAME_ROUTER, "ticksPerUpgrade", Defaults.TICKS_PER_UPGRADE,
                "Tick rate reduction per upgrade installed", 2, 10);
        propTicksPerUpgrade.setLanguageKey("gui.config.ticksPerUpgrade");
        Property propHardMinTicks = config.get(CATEGORY_NAME_ROUTER, "hardMinTicks", 2,
                "Hard minimum tick rate", 1, Integer.MAX_VALUE);
        propHardMinTicks.setLanguageKey("gui.config.hardMinTicks");
        Property propEcoTimeout = config.get(CATEGORY_NAME_ROUTER, "ecoTimeout", Defaults.ECO_TIMEOUT,
                "Idle time (ticks) before an eco-mode router goes into low power mode", 20, Integer.MAX_VALUE);
        propEcoTimeout.setLanguageKey("gui.config.ecoTimeout");
        Property propLowPowerTickRate = config.get(CATEGORY_NAME_ROUTER, "lowPowerTickRate", Defaults.LOW_POWER_INTERVAL,
                "Activation interval (ticks) for a low-power eco-mode router", 20, Integer.MAX_VALUE);
        propLowPowerTickRate.setLanguageKey("gui.config.lowPowerTickRate");

        Property propSender1BaseRange = config.get(CATEGORY_NAME_MODULE, "sender1BaseRange", Defaults.SENDER1_BASE_RANGE,
                "Sender Module Mk1 Base Range", 1, Integer.MAX_VALUE);
        propSender1BaseRange.setLanguageKey("gui.config.sender1BaseRange");
        Property propSender1MaxRange = config.get(CATEGORY_NAME_MODULE, "sender1MaxRange", Defaults.SENDER1_MAX_RANGE,
                "Sender Module Mk1 Max Range", 1, Integer.MAX_VALUE);
        propSender1MaxRange.setLanguageKey("gui.config.sender1MaxRange");

        Property propSender2BaseRange = config.get(CATEGORY_NAME_MODULE, "sender2BaseRange", Defaults.SENDER2_BASE_RANGE,
                "Sender Module Mk2 Base Range", 1, Integer.MAX_VALUE);
        propSender2BaseRange.setLanguageKey("gui.config.sender2BaseRange");
        Property propSender2MaxRange = config.get(CATEGORY_NAME_MODULE, "sender2MaxRange", Defaults.SENDER2_MAX_RANGE,
                "Sender Module Mk2 Max Range", 1, Integer.MAX_VALUE);
        propSender2MaxRange.setLanguageKey("gui.config.sender2MaxRange");

        Property propVacuumBaseRange = config.get(CATEGORY_NAME_MODULE, "vacuumBaseRange", Defaults.VACUUM_BASE_RANGE,
                "Vacuum Module Base Range", 1, Integer.MAX_VALUE);
        propVacuumBaseRange.setLanguageKey("gui.config.vacuumBaseRange");
        Property propVacuumMaxRange = config.get(CATEGORY_NAME_MODULE, "vacuumMaxRange", Defaults.VACUUM_MAX_RANGE,
                "Vacuum Module Max Range", 1, Integer.MAX_VALUE);
        propVacuumMaxRange.setLanguageKey("gui.config.vacuumMaxRange");

        Property propVacuumParticles = config.get(CATEGORY_NAME_MODULE, "vacuumParticles", Defaults.VACUUM_PARTICLES,
                "Show particles when Vacuum Module absorbs items");
        propVacuumParticles.setLanguageKey("gui.config.vacuumParticles");
        Property propSenderParticles = config.get(CATEGORY_NAME_MODULE, "senderParticles", Defaults.SENDER_PARTICLES,
                "Show particles when Sender Modules send items");
        propSenderParticles.setLanguageKey("gui.config.senderParticles");
        Property propPlacerParticles = config.get(CATEGORY_NAME_MODULE, "placerParticles", Defaults.PLACER_PARTICLES,
                "Show particles when Placer Module places a block");
        propPlacerParticles.setLanguageKey("gui.config.placerParticles");
        Property propBreakerParticles = config.get(CATEGORY_NAME_MODULE, "breakerParticles", Defaults.BREAKER_PARTICLES,
                "Show particles when Breaker Module breaks a block");
        propBreakerParticles.setLanguageKey("gui.config.breakerParticles");
        Property propExtruderSound = config.get(CATEGORY_NAME_MODULE, "extruderSound", Defaults.EXTRUDER_SOUND,
                "Play sounds when Extruder Module extends or contracts");
        propExtruderSound.setLanguageKey("gui.config.extruderSound");

        Property propStartWithGuide = config.get(CATEGORY_NAME_MISC, "startWithGuide", Defaults.START_WITH_GUIDE,
                "New players spawn with a guide book");
        propStartWithGuide.setLanguageKey("gui.config.startWithGuide");

        config.setCategoryPropertyOrder(CATEGORY_NAME_ROUTER, Arrays.asList(
                propBaseTickRate,
                propTicksPerUpgrade,
                propHardMinTicks,
                propConfigKey,
                propEcoTimeout,
                propLowPowerTickRate
        ).stream().map(Property::getName).collect(Collectors.toList()));

        config.setCategoryPropertyOrder(CATEGORY_NAME_MODULE, Arrays.asList(
                propSender1BaseRange,
                propSender1MaxRange,
                propSender2BaseRange,
                propSender2MaxRange,
                propVacuumBaseRange,
                propVacuumMaxRange,
                propSenderParticles,
                propVacuumParticles,
                propPlacerParticles,
                propBreakerParticles,
                propExtruderSound
        ).stream().map(Property::getName).collect(Collectors.toList()));

        config.setCategoryPropertyOrder(CATEGORY_NAME_MISC, Collections.singletonList(
                propStartWithGuide
        ).stream().map(Property::getName).collect(Collectors.toList()));

        if (readFieldsFromConfig) {
            baseTickRate = Math.max(1, propBaseTickRate.getInt(Defaults.BASE_TICK_RATE));
            ticksPerUpgrade = propTicksPerUpgrade.getInt(Defaults.TICKS_PER_UPGRADE);
            hardMinTickRate = propHardMinTicks.getInt();
            String s = propConfigKey.getString();
            configKey = s.length() > 0 ? propConfigKey.getString().charAt(0) : Defaults.CONFIG_KEY;
            ecoTimeout = propEcoTimeout.getInt();
            lowPowerTickRate = propLowPowerTickRate.getInt();
            sender1BaseRange = propSender1BaseRange.getInt();
            sender1MaxRange = propSender1MaxRange.getInt();
            sender2BaseRange = propSender2BaseRange.getInt();
            sender2MaxRange = propSender2MaxRange.getInt();
            vacuumBaseRange = propVacuumBaseRange.getInt();
            vacuumMaxRange = propVacuumMaxRange.getInt();
            senderParticles = propSenderParticles.getBoolean();
            vacuumParticles = propVacuumParticles.getBoolean();
            placerParticles = propPlacerParticles.getBoolean();
            breakerParticles = propBreakerParticles.getBoolean();
            extruderSound = propExtruderSound.getBoolean();
            startWithGuide = propStartWithGuide.getBoolean();
        }

        propBaseTickRate.set(baseTickRate);
        propTicksPerUpgrade.set(ticksPerUpgrade);
        propHardMinTicks.set(hardMinTickRate);
        propConfigKey.set(String.valueOf(configKey));
        propEcoTimeout.set(ecoTimeout);
        propLowPowerTickRate.set(lowPowerTickRate);
        propSender1BaseRange.set(sender1BaseRange);
        propSender1MaxRange.set(sender1MaxRange);
        propSender2BaseRange.set(sender2BaseRange);
        propSender2MaxRange.set(sender2MaxRange);
        propVacuumBaseRange.set(vacuumBaseRange);
        propVacuumMaxRange.set(vacuumMaxRange);
        propSenderParticles.set(senderParticles);
        propVacuumParticles.set(vacuumParticles);
        propPlacerParticles.set(placerParticles);
        propBreakerParticles.set(breakerParticles);
        propExtruderSound.set(extruderSound);
        propStartWithGuide.set(startWithGuide);

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
