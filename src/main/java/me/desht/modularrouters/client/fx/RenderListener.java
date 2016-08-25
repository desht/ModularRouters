package me.desht.modularrouters.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderListener {
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("modularrouters-particles");
        ParticleRenderDispatcher.dispatch();
        profiler.endSection();
    }
}
