package me.desht.modularrouters.client.item_beam;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public enum ItemBeamDispatcher {
    INSTANCE;

    private static final int MAX_SIZE = 250;

    private final List<ItemBeam> beams = new ArrayList<>();

    public void addBeam(ItemBeam itemBeam) {
        if (beams.size() < MAX_SIZE) {
            beams.add(itemBeam);
        }
    }

    public void tick() {
        for (Iterator<ItemBeam> iterator = beams.iterator(); iterator.hasNext(); ) {
            ItemBeam beam = iterator.next();
            beam.tick();
            if (beam.isExpired()) iterator.remove();
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        IProfiler profiler = Minecraft.getInstance().getProfiler();
        profiler.startSection("modularrouters-particles");
        GlStateManager.pushMatrix();

        GlStateManager.translated(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);
        for (ItemBeam beam: ItemBeamDispatcher.INSTANCE.beams) {
            beam.render(event.getPartialTicks());
        }

        GlStateManager.popMatrix();
        profiler.endSection();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            INSTANCE.tick();
        }
    }
}
