package me.desht.modularrouters.client.item_beam;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ItemBeam {
    private final Vec3d startPos;
    private final Vec3d endPos;
    private final ItemStack renderItem;
    private final int[] colors;
    private final int lifeTime;  // ticks
    private final boolean itemFade;
    private int ticksLived = 0;

    public ItemBeam(Vec3d startPos, Vec3d endPos, ItemStack renderItem, int color, int lifeTime, boolean itemFade) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.renderItem = renderItem;
        this.colors = decompose(color);
        this.lifeTime = lifeTime;
        this.itemFade = itemFade;
    }

    private int[] decompose(int color) {
        int[] res = new int[3];
        res[0] = color >> 16 & 0xff;
        res[1] = color >> 8  & 0xff;
        res[2] = color       & 0xff;
        return res;
    }

    public void tick() {
        ticksLived++;
    }

    public void render(float partialTicks) {
        float f = (ticksLived + partialTicks) / lifeTime;
        int alpha = (int)((1 - Math.abs(f - 0.5)) * 32 + 16);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture();
        BufferBuilder wr = Tessellator.getInstance().getBuffer();

        GlStateManager.lineWidth(10);
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(startPos.x, startPos.y, startPos.z).color(colors[0], colors[1], colors[2], alpha).endVertex();
        wr.pos(interp(startPos.x, endPos.x, 1f), interp(startPos.y, endPos.y, 1f), interp(startPos.z, endPos.z, 1f)).color(colors[0], colors[1], colors[2], itemFade ? alpha / 2 : alpha).endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.lineWidth(3);
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(startPos.x, startPos.y, startPos.z).color(colors[0], colors[1], colors[2], alpha).endVertex();
        wr.pos(interp(startPos.x, endPos.x, 1f), interp(startPos.y, endPos.y, 1f), interp(startPos.z, endPos.z, 1f)).color(colors[0], colors[1], colors[2], itemFade ? alpha / 2 : alpha).endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.enableTexture();

        double ix = interp(startPos.x, endPos.x, f);
        double iy = interp(startPos.y, endPos.y, f);
        double iz = interp(startPos.z, endPos.z, f);
        float yRot = f * 360;

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();

        GlStateManager.translated(ix, iy - 0.15, iz);
        GlStateManager.rotatef(yRot, 0.25f, 1, 0);
        if (itemFade) {
            World w = Minecraft.getInstance().world;
            GlStateManager.scalef(1.25f - f, 1.25f - f, 1.25f - f);
            if (f > 0.9) w.addParticle(ParticleTypes.PORTAL, endPos.x, endPos.y, endPos.z, 0.5 - w.rand.nextDouble(), -0.5, 0.5 - w.rand.nextDouble());
        }
        Minecraft.getInstance().getItemRenderer().renderItem(renderItem, ItemCameraTransforms.TransformType.GROUND);

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    boolean isExpired() {
        return ticksLived >= lifeTime;
    }

    private double interp(double p1, double p2, float f) {
        return p1 + (p2 - p1) * f;
    }
}
