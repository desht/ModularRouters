package me.desht.modularrouters.integration.top;

import net.minecraft.resources.ResourceLocation;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class TOPCompatibility {
    private static boolean registered;

    static final ResourceLocation ELEMENT_MODULE_ITEM = RL("module");

    public static void register() {
        if (registered)
            return;
        registered = true;
//        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> (Function<ITheOneProbe, Void>) iTheOneProbe -> {
//            ModularRouters.LOGGER.info("Enabled support for The One Probe");
//
//            iTheOneProbe.registerElementFactory(new ElementModule.Factory());
//
//            iTheOneProbe.registerProvider(new IProbeInfoProvider() {
//                @Override
//                public ResourceLocation getID() {
//                    return RL("default");
//                }
//
//                @Override
//                public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData iProbeHitData) {
//                    if (blockState.getBlock() instanceof ModularRouterBlock) {
//                        TOPInfoProvider.addProbeInfo(probeMode, probeInfo, player, world, blockState, iProbeHitData);
//                    }
//                }
//            });
//            return null;
//        });
    }
}