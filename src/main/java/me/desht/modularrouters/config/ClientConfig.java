package me.desht.modularrouters.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public class Misc {
        ForgeConfigSpec.BooleanValue alwaysShowModuleSettings;
        ForgeConfigSpec.BooleanValue moduleGuiBackgroundTint;
    }

    public final Misc misc = new Misc();

    ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Misc");
        misc.alwaysShowModuleSettings = builder.comment("Should module tooltips always show module settings (without needing to hold Shift)?")
                .translation("gui.config.alwaysShowSettings")
                .define("alwaysShowSettings", true);
        misc.moduleGuiBackgroundTint = builder.comment("Should module GUI's be tinted according to the module item colour?")
                .translation("gui.config.moduleGuiBackgroundTint")
                .define("moduleGuiBackgroundTint", true);
        builder.pop();
    }
}
