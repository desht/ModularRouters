package me.desht.modularrouters.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderListener {
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        IProfiler profiler = Minecraft.getInstance().getProfiler();
        profiler.startSection("modularrouters-particles");
        ParticleRenderDispatcher.dispatch();
        profiler.endSection();
    }
}
