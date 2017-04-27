package me.desht.modularrouters.integration.top;

import mcjty.theoneprobe.api.*;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nullable;

public class TOPCompatibility {
    private static boolean registered;

    public static int ELEMENT_MODULE_ITEM;

    public static void register() {
        if (registered)
            return;
        registered = true;
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "me.desht.modularrouters.integration.top.TOPCompatibility$GetTheOneProbe");
    }

    public static class GetTheOneProbe implements com.google.common.base.Function<ITheOneProbe, Void> {
        public static ITheOneProbe probe;

        @Nullable
        @Override
        public Void apply(ITheOneProbe theOneProbe) {
            probe = theOneProbe;
            ModularRouters.logger.info("Enabled support for The One Probe");

            ELEMENT_MODULE_ITEM = probe.registerElementFactory(ElementModule::new);

            probe.registerProvider(new IProbeInfoProvider() {
                @Override
                public String getID() {
                    return ModularRouters.MODID + ":default";
                }

                @Override
                public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
                    if (blockState.getBlock() instanceof TOPInfoProvider) {
                        TOPInfoProvider provider = (TOPInfoProvider) blockState.getBlock();
                        provider.addProbeInfo(mode, probeInfo, player, world, blockState, data);
                    }

                }
            });
            return null;
        }
    }
}