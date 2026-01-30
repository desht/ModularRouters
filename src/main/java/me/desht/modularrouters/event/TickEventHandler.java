package me.desht.modularrouters.event;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = ModularRouters.MODID)
public class TickEventHandler {

    public static long TickCounter = 0;

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel().dimension() == Level.OVERWORLD && !event.getLevel().isClientSide()) {
            TickCounter++;
        }
    }
}
