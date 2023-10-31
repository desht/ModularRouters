package me.desht.modularrouters.event;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID)
public class TickEventHandler {

    public static long TickCounter = 0;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.level.dimension() == Level.OVERWORLD && event.phase == TickEvent.Phase.END) {
            TickCounter++;
        }
    }
}