package me.desht.modularrouters.config;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;
import java.util.stream.Collectors;

public class ConfigHelper {
    private static net.minecraftforge.fml.config.ModConfig clientConfig;
    private static net.minecraftforge.fml.config.ModConfig commonConfig;

    static void refreshClient(net.minecraftforge.fml.config.ModConfig config) {
        clientConfig = config;

        ClientConfig client = ConfigHolder.client;
        MRConfig.Client.Misc.alwaysShowModuleSettings = client.misc.alwaysShowModuleSettings.get();
        MRConfig.Client.Misc.moduleGuiBackgroundTint = client.misc.moduleGuiBackgroundTint.get();
        MRConfig.Client.Misc.renderFlyingItems = client.misc.renderFlyingItems.get();
        MRConfig.Client.Misc.heldRouterShowsCamoRouters = client.misc.heldRouterShowsCamoRouters.get();

        ModularRouters.LOGGER.info("Client config re-baked");
    }

    static void refreshCommon(net.minecraftforge.fml.config.ModConfig config) {
        commonConfig = config;

        CommonConfig common = ConfigHolder.common;
        MRConfig.Common.Module.sender1BaseRange = common.module.sender1BaseRange.get();
        MRConfig.Common.Module.sender1MaxRange = common.module.sender1MaxRange.get();
        MRConfig.Common.Module.sender2BaseRange = common.module.sender2BaseRange.get();
        MRConfig.Common.Module.sender2MaxRange = common.module.sender2MaxRange.get();
        MRConfig.Common.Module.vacuumBaseRange = common.module.vacuumBaseRange.get();
        MRConfig.Common.Module.vacuumMaxRange = common.module.vacuumMaxRange.get();
        MRConfig.Common.Module.extruder1BaseRange = common.module.extruder1BaseRange.get();
        MRConfig.Common.Module.extruder1MaxRange = common.module.extruder1MaxRange.get();
        MRConfig.Common.Module.extruder2BaseRange = common.module.extruder2BaseRange.get();
        MRConfig.Common.Module.extruder2MaxRange = common.module.extruder2MaxRange.get();
        MRConfig.Common.Module.puller2BaseRange = common.module.puller2BaseRange.get();
        MRConfig.Common.Module.puller2MaxRange = common.module.puller2MaxRange.get();
        MRConfig.Common.Module.fluid2BaseRange = common.module.fluid2BaseRange.get();
        MRConfig.Common.Module.fluid2MaxRange = common.module.fluid2MaxRange.get();
        MRConfig.Common.Module.senderParticles = common.module.senderParticles.get();
        MRConfig.Common.Module.pullerParticles = common.module.pullerParticles.get();
        MRConfig.Common.Module.placerParticles = common.module.placerParticles.get();
        MRConfig.Common.Module.breakerParticles = common.module.breakerParticles.get();
        MRConfig.Common.Module.vacuumParticles = common.module.vacuumParticles.get();
        MRConfig.Common.Module.flingerEffects = common.module.flingerEffects.get();
        MRConfig.Common.Module.extruderSound = common.module.extruderSound.get();
        MRConfig.Common.Module.extruderPushEntities = common.module.extruderPushEntities.get();
        MRConfig.Common.Module.breakerHarvestLevelLimit = common.module.breakerHarvestLevelLimit.get();
        MRConfig.Common.Module.activatorEntityBlacklist = common.module.activatorEntityBlacklist.get()
                .stream().map(resourceName -> new ResourceLocation(resourceName.toLowerCase(Locale.ROOT))).collect(Collectors.toSet());

        MRConfig.Common.Router.baseTickRate = common.router.baseTickRate.get();
        MRConfig.Common.Router.ticksPerUpgrade = common.router.ticksPerUpgrade.get();
        MRConfig.Common.Router.hardMinTickRate = common.router.hardMinTickRate.get();
        MRConfig.Common.Router.ecoTimeout = common.router.ecoTimeout.get();
        MRConfig.Common.Router.lowPowerTickRate = common.router.lowPowerTickRate.get();
        MRConfig.Common.Router.fluidBaseTransferRate = common.router.fluidBaseTransferRate.get();
        MRConfig.Common.Router.fluidMaxTransferRate = common.router.fluidMaxTransferRate.get();
        MRConfig.Common.Router.mBperFluidUpgade = common.router.mBperFluidUpgade.get();
        MRConfig.Common.Router.fePerEnergyUpgrade = common.router.fePerEnergyUpgrade.get();
        MRConfig.Common.Router.feXferPerEnergyUpgrade = common.router.feXferPerEnergyUpgrade.get();

        MRConfig.Common.EnergyCosts.activatorModuleEnergyCost = common.energyCosts.activatorModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.breakerModuleEnergyCost = common.energyCosts.breakerModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.detectorModuleEnergyCost = common.energyCosts.detectorModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.distributorModuleEnergyCost = common.energyCosts.distributorModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.dropperModuleEnergyCost = common.energyCosts.dropperModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.energydistributorModuleEnergyCost = common.energyCosts.energydistributorModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.energyoutputModuleEnergyCost = common.energyCosts.energyoutputModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.extruderModule1EnergyCost = common.energyCosts.extruderModule1EnergyCost.get();
        MRConfig.Common.EnergyCosts.extruderModule2EnergyCost = common.energyCosts.extruderModule2EnergyCost.get();
        MRConfig.Common.EnergyCosts.flingerModuleEnergyCost = common.energyCosts.flingerModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.fluidModuleEnergyCost = common.energyCosts.fluidModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.fluidModule2EnergyCost = common.energyCosts.fluidModule2EnergyCost.get();
        MRConfig.Common.EnergyCosts.placerModuleEnergyCost = common.energyCosts.placerModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.playerModuleEnergyCost = common.energyCosts.playerModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.pullerModule1EnergyCost = common.energyCosts.pullerModule1EnergyCost.get();
        MRConfig.Common.EnergyCosts.pullerModule2EnergyCost = common.energyCosts.pullerModule2EnergyCost.get();
        MRConfig.Common.EnergyCosts.senderModule1EnergyCost = common.energyCosts.senderModule1EnergyCost.get();
        MRConfig.Common.EnergyCosts.senderModule2EnergyCost = common.energyCosts.senderModule2EnergyCost.get();
        MRConfig.Common.EnergyCosts.senderModule3EnergyCost = common.energyCosts.senderModule3EnergyCost.get();
        MRConfig.Common.EnergyCosts.vacuumModuleEnergyCost = common.energyCosts.vacuumModuleEnergyCost.get();
        MRConfig.Common.EnergyCosts.voidModuleEnergyCost = common.energyCosts.voidModuleEnergyCost.get();

        ModularRouters.LOGGER.info("Common config re-baked");
    }
}
