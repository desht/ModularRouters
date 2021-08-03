package me.desht.modularrouters.integration.top;

import mcjty.theoneprobe.api.*;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.ModularRouterBlock;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.InterModComms;

import java.util.function.Function;

public class TOPCompatibility {
    private static boolean registered;

    static ResourceLocation ELEMENT_MODULE_ITEM = MiscUtil.RL("module");

    public static void register() {
        if (registered)
            return;
        registered = true;
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> (Function<ITheOneProbe, Void>) iTheOneProbe -> {
            ModularRouters.LOGGER.info("Enabled support for The One Probe");

            iTheOneProbe.registerElementFactory(new ElementModule.Factory());

            iTheOneProbe.registerProvider(new IProbeInfoProvider() {
                @Override
                public String getID() {
                    return ModularRouters.MODID + ":default";
                }

                @Override
                public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData iProbeHitData) {
                    if (blockState.getBlock() instanceof ModularRouterBlock) {
                        TOPInfoProvider.addProbeInfo(probeMode, probeInfo, player, world, blockState, iProbeHitData);
                    }
                }
            });
            return null;
        });
    }
}