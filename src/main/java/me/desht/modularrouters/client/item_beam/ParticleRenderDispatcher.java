package me.desht.modularrouters.client.item_beam;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.profiler.IProfiler;
import org.lwjgl.opengl.GL11;

public class ParticleRenderDispatcher {

    public static int sparkleFxCount = 0;
    public static int fakeSparkleFxCount = 0;

    public static void dispatch() {
        Tessellator tessellator = Tessellator.getInstance();

        IProfiler profiler = Minecraft.getInstance().getProfiler();

        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
        GlStateManager.disableLighting();

        profiler.startSection("sparkle");
//        FXSparkle.dispatchQueuedRenders(tessellator);
        profiler.endSection();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GL11.glPopAttrib();
    }

}
