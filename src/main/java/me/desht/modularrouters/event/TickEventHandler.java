package me.desht.modularrouters.event;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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