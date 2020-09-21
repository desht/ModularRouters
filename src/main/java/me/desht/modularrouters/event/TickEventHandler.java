package me.desht.modularrouters.event;

import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TickEventHandler {

    public static long TickCounter = 0;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.getDimensionKey() == World.OVERWORLD && event.phase == TickEvent.Phase.END) {
            TickCounter++;
        }
    }
}
