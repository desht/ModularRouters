package me.desht.modularrouters.event;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TickEventHandler {

    public static long TickCounter = 0;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.getDimension().getType() == DimensionType.OVERWORLD && event.phase == TickEvent.Phase.END) {
            TickCounter++;
        }
    }
}
