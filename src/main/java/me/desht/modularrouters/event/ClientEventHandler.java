package me.desht.modularrouters.event;

import me.desht.modularrouters.util.Scheduler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Scheduler.client().tick();
        }
    }
}