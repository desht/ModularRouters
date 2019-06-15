package me.desht.modularrouters.integration.top;

import mcjty.theoneprobe.api.*;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;

import java.util.function.Function;

public class TOPCompatibility {
    private static boolean registered;

    static int ELEMENT_MODULE_ITEM;

    public static void register() {
        if (registered)
            return;
        registered = true;
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> (Function<ITheOneProbe, Void>) iTheOneProbe -> {
            ModularRouters.LOGGER.info("Enabled support for The One Probe");

            ELEMENT_MODULE_ITEM = iTheOneProbe.registerElementFactory(ElementModule::new);

            iTheOneProbe.registerProvider(new IProbeInfoProvider() {
                @Override
                public String getID() {
                    return ModularRouters.MODID + ":default";
                }

                @Override
                public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData iProbeHitData) {
                    if (blockState.getBlock() instanceof BlockItemRouter) {
                        TOPInfoProvider.addProbeInfoItemRouter(probeMode, probeInfo, player, world, blockState, iProbeHitData);
                    }
                }
            });
            return null;
        });
    }
}