package me.desht.modularrouters.integration.top;

// todo 1.13
public class TOPCompatibility {
//    private static boolean registered;
//
//    public static int ELEMENT_MODULE_ITEM;
//
//    public static void register() {
//        if (registered)
//            return;
//        registered = true;
//        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "me.desht.modularrouters.integration.top.TOPCompatibility$GetTheOneProbe");
//    }
//
//    public static class GetTheOneProbe implements com.google.common.base.Function<ITheOneProbe, Void> {
//        public static ITheOneProbe probe;
//
//        @Nullable
//        @Override
//        public Void apply(ITheOneProbe theOneProbe) {
//            probe = theOneProbe;
//            ModularRouters.LOGGER.info("Enabled support for The One Probe");
//
//            ELEMENT_MODULE_ITEM = probe.registerElementFactory(ElementModule::new);
//
//            probe.registerProvider(new IProbeInfoProvider() {
//                @Override
//                public String getID() {
//                    return ModularRouters.MODID + ":default";
//                }
//
//                @Override
//                public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
//                    if (blockState.getBlock() instanceof TOPInfoProvider) {
//                        TOPInfoProvider provider = (TOPInfoProvider) blockState.getBlock();
//                        provider.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//                    }
//
//                }
//            });
//            return null;
//        }
//    }
}