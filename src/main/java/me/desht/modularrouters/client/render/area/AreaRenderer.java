package me.desht.modularrouters.client.render.area;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.modularrouters.client.render.ModRenderTypes;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;

import java.util.Set;

public class AreaRenderer {
    private final Set<BlockPos> showingPositions;
    private final int[] cols;
    private final float size;

    AreaRenderer(Set<BlockPos> area, int color, float size) {
        this.showingPositions = area;
        this.cols = new int[] { (color >> 24) & 0xFF, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF };
        this.size = size;
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer) {
        render(matrixStack, buffer.getBuffer(ModRenderTypes.BLOCK_HILIGHT_FACE));
        RenderSystem.disableDepthTest();
        buffer.finish(ModRenderTypes.BLOCK_HILIGHT_FACE);

        render(matrixStack, buffer.getBuffer(ModRenderTypes.BLOCK_HILIGHT_LINE));
        RenderSystem.disableDepthTest();
        buffer.finish(ModRenderTypes.BLOCK_HILIGHT_LINE);
    }

    private void render(MatrixStack matrixStack, IVertexBuilder builder) {
        for (BlockPos pos : showingPositions) {
            matrixStack.push();
            double start = (1 - size) / 2.0;
            matrixStack.translate(pos.getX() + start, pos.getY() + start, pos.getZ() + start);
            Matrix4f posMat = matrixStack.getLast().getMatrix();
            addVertices(builder, posMat);
            matrixStack.pop();
        }
    }

    private void addVertices(IVertexBuilder wr, Matrix4f posMat) {
        wr.pos(posMat, 0, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, 0, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, size, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, size, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

        wr.pos(posMat, size, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, size, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, 0, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, 0, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

        wr.pos(posMat, 0, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, 0, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, 0, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, 0, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

        wr.pos(posMat, size, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, size, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, size, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, size, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

        wr.pos(posMat, 0, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, size, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, size, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, 0, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

        wr.pos(posMat, 0, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, size, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, size, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.pos(posMat, 0, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
    }
}
