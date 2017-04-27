package me.desht.modularrouters.config;

import me.desht.modularrouters.ModularRouters;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = ModularRouters.MODID)
@Config.LangKey(value = "gui.config.mainTitle")
public class ConfigHandler {
    @Config.Name("module")
    @Config.LangKey("gui.config.ctgy.module")
    public static Module module = new Module();

    @Config.Name("router")
    @Config.LangKey("gui.config.ctgy.router")
    public static Router router = new Router();

    @Config.Name("misc")
    @Config.LangKey("gui.config.ctgy.misc")
    public static Misc misc = new Misc();

    public static char getConfigKey() {
        return router.configKey.isEmpty() ? 'c' : router.configKey.charAt(0);
    }

    public static class Module {
        @Config.LangKey("gui.config.sender1BaseRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Base range for Sender Mk1 (no range upgrades)")
        public int sender1BaseRange = 8;

        @Config.LangKey("gui.config.sender1MaxRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Max range for Sender Mk1")
        public int sender1MaxRange = 16;

        @Config.LangKey("gui.config.sender2BaseRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Base range for Sender Mk2 (no range upgrades)")
        public int sender2BaseRange = 24;

        @Config.LangKey("gui.config.sender2MaxRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Max range for Sender Mk2")
        public int sender2MaxRange= 48;

        @Config.LangKey("gui.config.vacuumBaseRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Base range for Vacuum (no range upgrades)")
        public int vacuumBaseRange = 6;

        @Config.LangKey("gui.config.vacuumMaxRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Max range for Vacuum")
        public int vacuumMaxRange = 12;

        @Config.LangKey("gui.config.extruderBaseRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Base range for Extruder Mk1 (no range upgrades)")
        public int extruderBaseRange = 12;

        @Config.LangKey("gui.config.extruderMaxRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Max range for Extruder Mk1")
        public int extruderMaxRange = 24;

        @Config.LangKey("gui.config.extruder2BaseRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Base range for Extruder Mk2 (no range upgrades)")
        public int extruder2BaseRange = 24;

        @Config.LangKey("gui.config.extruder2MaxRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Max range for Extruder Mk2")
        public int extruder2MaxRange = 48;

        @Config.LangKey("gui.config.puller2BaseRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Base range for Puller Mk2 (no range upgrades)")
        public int puller2BaseRange = 12;

        @Config.LangKey("gui.config.puller2MaxRange")
        @Config.RangeInt(min = 1)
        @Config.Comment("Max range for Puller Mk2")
        public int puller2MaxRange = 24;

        @Config.LangKey("gui.config.senderParticles")
        @Config.Comment("Should Sender modules show particle effects when sending?")
        public boolean senderParticles = true;

        @Config.LangKey("gui.config.pullerParticles")
        @Config.Comment("Should Puller modules show particle effects when pulling?")
        public boolean pullerParticles = true;

        @Config.LangKey("gui.config.placerParticles")
        @Config.Comment("Should Placer modules show particle effects when placing a block?")
        public boolean placerParticles = false;

        @Config.LangKey("gui.config.breakerParticles")
        @Config.Comment("Should Breaker modules show block particle effects when breaking a block?")
        public boolean breakerParticles = true;

        @Config.LangKey("gui.config.vacuumParticles")
        @Config.Comment("Should Vacuum modules show particle effects when absorbing items?")
        public boolean vacuumParticles = true;

        @Config.LangKey("gui.config.flingerEffects")
        @Config.Comment("Should Flinger modules show smoke effects & play a sound when flinging items?")
        public boolean flingerEffects = true;

        @Config.LangKey("gui.config.extruderSound")
        @Config.Comment("Should Extruder (Mk1 & 2) modules play a sound when placing blocks?")
        public boolean extruderSound = true;
    }

    public static class Router {
        @Config.LangKey("gui.config.configKey")
        @Config.Comment("Key to press while mousing over a module in the Item Router GUI to configure the module")
        public String configKey = "c";

        @Config.LangKey("gui.config.baseTickRate")
        @Config.RangeInt(min = 1)
        @Config.Comment("Base tick interval (in server ticks) for a router; router will run this often")
        public int baseTickRate = 20;

        @Config.LangKey("gui.config.ticksPerUpgrade")
        @Config.RangeInt(min = 1, max = 20)
        @Config.Comment("Number of ticks by which 1 Speed Upgrade will reduce the router's tick interval")
        public int ticksPerUpgrade = 2;

        @Config.LangKey("gui.config.hardMinTicks")
        @Config.RangeInt(min = 1)
        @Config.Comment("Hard minimum tick interval for a router regardless of Speed Upgrades")
        public int hardMinTickRate = 2;

        @Config.LangKey("gui.config.ecoTimeout")
        @Config.RangeInt(min = 20)
        @Config.Comment("Router with eco mode enabled will go into low-power mode if idle for this many server ticks")
        public int ecoTimeout = 100;

        @Config.LangKey("gui.config.lowPowerTickRate")
        @Config.RangeInt(min = 20)
        @Config.Comment("Tick interval for an eco-mode router which has gone into low-power mode")
        public int lowPowerTickRate = 100;

        @Config.LangKey("gui.config.fluidBaseTransferRate")
        @Config.RangeInt(min = 1)
        @Config.Comment("Base fluid transfer rate (mB/t in each direction) for a router")
        public int fluidBaseTransferRate = 50;

        @Config.LangKey("gui.config.fluidMaxTransferRate")
        @Config.RangeInt(min = 1)
        @Config.Comment("Max fluid transfer rate (mB/t in each direction) for a router")
        public int fluidMaxTransferRate = 400;

        @Config.LangKey("gui.config.mbPerFluidUpgrade")
        @Config.RangeInt(min = 1)
        @Config.Comment("Fluid transfer rate increase per Fluid Transfer Upgrade")
        public int mBperFluidUpgrade = 10;
    }

    public static class Misc {
        @Config.LangKey("gui.config.startWithGuide")
        @Config.Comment("Should new players start with a copy of the Modular Routers guidebook?")
        public boolean startWithGuide = false;
    }

    @Mod.EventBusSubscriber
    public static class ConfigSyncHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(ModularRouters.MODID)) {
                ConfigManager.sync(ModularRouters.MODID, Config.Type.INSTANCE);
                ModularRouters.logger.info("Configuration has been saved.");
            }
        }
    }
}
