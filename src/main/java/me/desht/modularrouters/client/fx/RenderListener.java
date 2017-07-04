package me.desht.modularrouters.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RenderListener {
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("modularrouters-particles");
        ParticleRenderDispatcher.dispatch();
        profiler.endSection();
    }
}
