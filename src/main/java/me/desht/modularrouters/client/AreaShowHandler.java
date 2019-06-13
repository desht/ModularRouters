package me.desht.modularrouters.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class AreaShowHandler {
    private final double size;
    private final AreaShowManager.CompiledPosition cp;
    private int renderList;

    AreaShowHandler(AreaShowManager.CompiledPosition cp) {
        this.cp = cp;
        this.size = 0.5;
        compileRenderList();
    }

    private void compileRenderList() {
        renderList = GL11.glGenLists(1);
        GL11.glNewList(renderList, GL11.GL_COMPILE);

        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        double start = (1 - size) / 2.0;

        ClientPlayerEntity player = Minecraft.getInstance().player;
        for (BlockPos pos : cp.getPositions()) {
            wr.setTranslation(pos.getX() + start, pos.getY() + start /*- player.getEyeHeight(player.getPose())*/, pos.getZ() + start);
            int color = cp.getColour(pos);
            int r = (color & 0xFF0000) >> 16;
            int g = (color & 0xFF00) >> 8;
            int b = color & 0xFF;
            int alpha;

            alpha = getFaceAlpha(cp, pos, Direction.NORTH);
            wr.pos(0, 0, 0).color(r, g, b, alpha).endVertex();
            wr.pos(0, size, 0).color(r, g, b, alpha).endVertex();
            wr.pos(size, size, 0).color(r, g, b, alpha).endVertex();
            wr.pos(size, 0, 0).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.SOUTH);
            wr.pos(size, 0, size).color(r, g, b, alpha).endVertex();
            wr.pos(size, size, size).color(r, g, b, alpha).endVertex();
            wr.pos(0, size, size).color(r, g, b, alpha).endVertex();
            wr.pos(0, 0, size).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.WEST);
            wr.pos(0, 0, 0).color(r, g, b, alpha).endVertex();
            wr.pos(0, 0, size).color(r, g, b, alpha).endVertex();
            wr.pos(0, size, size).color(r, g, b, alpha).endVertex();
            wr.pos(0, size, 0).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.EAST);
            wr.pos(size, size, 0).color(r, g, b, alpha).endVertex();
            wr.pos(size, size, size).color(r, g, b, alpha).endVertex();
            wr.pos(size, 0, size).color(r, g, b, alpha).endVertex();
            wr.pos(size, 0, 0).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.DOWN);
            wr.pos(0, 0, 0).color(r, g, b, alpha).endVertex();
            wr.pos(size, 0, 0).color(r, g, b, alpha).endVertex();
            wr.pos(size, 0, size).color(r, g, b, alpha).endVertex();
            wr.pos(0, 0, size).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.UP);
            wr.pos(0, size, size).color(r, g, b, alpha).endVertex();
            wr.pos(size, size, size).color(r, g, b, alpha).endVertex();
            wr.pos(size, size, 0).color(r, g, b, alpha).endVertex();
            wr.pos(0, size, 0).color(r, g, b, alpha).endVertex();
        }

        Tessellator.getInstance().draw();

        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        RenderHelper.glColorHex(0X404040, 128);

        for (BlockPos pos : cp.getPositions()) {
            wr.setTranslation(pos.getX() + start, pos.getY() + start /*- player.getEyeHeight(player.getPose())*/, pos.getZ() + start);

            wr.pos(0, 0, 0).endVertex();
            wr.pos(0, size, 0).endVertex();
            wr.pos(size, size, 0).endVertex();
            wr.pos(size, 0, 0).endVertex();

            wr.pos(size, 0, size).endVertex();
            wr.pos(size, size, size).endVertex();
            wr.pos(0, size, size).endVertex();
            wr.pos(0, 0, size).endVertex();

            wr.pos(0, 0, 0).endVertex();
            wr.pos(0, 0, size).endVertex();
            wr.pos(0, size, size).endVertex();
            wr.pos(0, size, 0).endVertex();

            wr.pos(size, size, 0).endVertex();
            wr.pos(size, size, size).endVertex();
            wr.pos(size, 0, size).endVertex();
            wr.pos(size, 0, 0).endVertex();

            wr.pos(0, 0, 0).endVertex();
            wr.pos(size, 0, 0).endVertex();
            wr.pos(size, 0, size).endVertex();
            wr.pos(0, 0, size).endVertex();

            wr.pos(0, size, size).endVertex();
            wr.pos(size, size, size).endVertex();
            wr.pos(size, size, 0).endVertex();
            wr.pos(0, size, 0).endVertex();
        }

        wr.setTranslation(0, 0, 0);
        Tessellator.getInstance().draw();
        GL11.glEndList();
    }

    private int getFaceAlpha(AreaShowManager.CompiledPosition cp, BlockPos pos, Direction face) {
        return cp.checkFace(pos, face) ? 224 : 64;
    }

    void render() {
        GL11.glCallList(renderList);
    }
}
