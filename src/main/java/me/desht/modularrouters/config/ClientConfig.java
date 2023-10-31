package me.desht.modularrouters.config;


import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public static class Misc {
        public ModConfigSpec.BooleanValue heldRouterShowsCamoRouters;
        public ModConfigSpec.BooleanValue alwaysShowModuleSettings;
        public ModConfigSpec.BooleanValue moduleGuiBackgroundTint;
        public ModConfigSpec.BooleanValue renderFlyingItems;
    }

    public static class Sound {
        public ModConfigSpec.DoubleValue bleepVolume;
    }

    public final Misc misc = new Misc();
    public final Sound sound = new Sound();

    ClientConfig(ModConfigSpec.Builder builder) {
        builder.push("Misc");
        misc.alwaysShowModuleSettings = builder.comment("Should module tooltips always show module settings (without needing to hold Shift)?")
                .translation("gui.config.alwaysShowSettings")
                .define("alwaysShowSettings", true);
        misc.moduleGuiBackgroundTint = builder.comment("Should module GUI's be tinted according to the module item colour?")
                .translation("modularrouters.gui.config.moduleGuiBackgroundTint")
                .define("moduleGuiBackgroundTint", true);
        misc.renderFlyingItems = builder.comment("Should items being transferred be rendered in-world? Looks good, but many items may incur an FPS hit.")
                .translation("gui.config.renderFlyingItems")
                .define("renderFlyingItems", true);
        misc.heldRouterShowsCamoRouters = builder.comment("When holding an Item Router, should nearby camouflaged routers be highlighted?")
                .translation("gui.config.heldRouterShowsCamoRouters")
                .define("heldRouterShowsCamoRouters", true);
        builder.pop();

        builder.push("Sound");
        sound.bleepVolume = builder.comment("Volume of the bleep played when various operations are done with modules/upgrades/etc. such as binding to an inventory, setting camo...")
                .translation("gui.config.moduleBindVolume")
                .defineInRange("bleepVolume", 0.5, 0.0, 2.0);
        builder.pop();
    }
}
