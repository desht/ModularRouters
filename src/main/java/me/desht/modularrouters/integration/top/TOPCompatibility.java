package me.desht.modularrouters.integration.top;

public class TOPCompatibility {
//    private static boolean registered;
//
//    static int ELEMENT_MODULE_ITEM;
//
//    public static void register() {
//        if (registered)
//            return;
//        registered = true;
//        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> (Function<ITheOneProbe, Void>) iTheOneProbe -> {
//            ModularRouters.LOGGER.info("Enabled support for The One Probe");
//
//            ELEMENT_MODULE_ITEM = iTheOneProbe.registerElementFactory(ElementModule::new);
//
//            iTheOneProbe.registerProvider(new IProbeInfoProvider() {
//                @Override
//                public String getID() {
//                    return ModularRouters.MODID + ":default";
//                }
//
//                @Override
//                public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData iProbeHitData) {
//                    if (blockState.getBlock() instanceof BlockItemRouter) {
//                        TOPInfoProvider.addProbeInfo(probeMode, probeInfo, player, world, blockState, iProbeHitData);
//                    }
//                }
//            });
//            return null;
//        });
//    }
}