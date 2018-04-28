package me.desht.modularrouters.event;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public class TickEventHandler {

    public static long TickCounter = 0;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.provider.getDimension() == 0 && event.phase == TickEvent.Phase.END) {
            TickCounter++;
        }
    }
}
