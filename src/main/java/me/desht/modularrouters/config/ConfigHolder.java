package me.desht.modularrouters.config;

import me.desht.modularrouters.ModularRouters;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHolder {
    public static ClientConfig client;
    public static CommonConfig common;
    private static ModConfigSpec configCommonSpec;
    private static ModConfigSpec configClientSpec;

    public static void init() {
        final Pair<ClientConfig, ModConfigSpec> spec1 = new ModConfigSpec.Builder().configure(ClientConfig::new);
        client = spec1.getLeft();
        configClientSpec = spec1.getRight();

        final Pair<CommonConfig, ModConfigSpec> spec2 = new ModConfigSpec.Builder().configure(CommonConfig::new);
        common = spec2.getLeft();
        configCommonSpec = spec2.getRight();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHolder.configCommonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHolder.configClientSpec);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigHolder::onConfigChanged);
    }

    private static void onConfigChanged(final ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == ConfigHolder.configClientSpec) {
            refreshClient();
        } else if (config.getSpec() == ConfigHolder.configCommonSpec) {
            refreshCommon();
        }
    }

    static void refreshClient() {
        // nothing for now
    }

    static void refreshCommon() {
        ModularRouters.clearDimensionBlacklist();
    }
}
