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
        // field_234918_g_ = OVERWORLD
        if (event.world.func_234923_W_() == World.field_234918_g_ && event.phase == TickEvent.Phase.END) {
            TickCounter++;
        }
    }
}
