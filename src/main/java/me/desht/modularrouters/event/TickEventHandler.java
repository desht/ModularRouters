package me.desht.modularrouters.event;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public class TickEventHandler {

    public static long TickCounter = 0;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (MiscUtil.getDimensionForWorld(event.world) == 0 && event.phase == TickEvent.Phase.END) {
            TickCounter++;
        }
    }
}
