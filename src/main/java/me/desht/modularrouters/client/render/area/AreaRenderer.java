package me.desht.modularrouters.client.render.area;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import me.desht.modularrouters.client.render.ModRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

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

    public void render(PoseStack matrixStack, MultiBufferSource.BufferSource buffer) {
        render(matrixStack, buffer.getBuffer(ModRenderTypes.BLOCK_HILIGHT_FACE));
        RenderSystem.disableDepthTest();
        buffer.endBatch(ModRenderTypes.BLOCK_HILIGHT_FACE);

        render(matrixStack, buffer.getBuffer(ModRenderTypes.BLOCK_HILIGHT_LINE));
        RenderSystem.disableDepthTest();
        buffer.endBatch(ModRenderTypes.BLOCK_HILIGHT_LINE);
    }

    private void render(PoseStack matrixStack, VertexConsumer builder) {
        for (BlockPos pos : showingPositions) {
            matrixStack.pushPose();
            double start = (1 - size) / 2.0;
            matrixStack.translate(pos.getX() + start, pos.getY() + start, pos.getZ() + start);
            Matrix4f posMat = matrixStack.last().pose();
            addVertices(builder, posMat);
            matrixStack.popPose();
        }
    }

    private void addVertices(VertexConsumer wr, Matrix4f posMat) {
        wr.vertex(posMat, 0, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, 0, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, size, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, size, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

        wr.vertex(posMat, size, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, size, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, 0, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, 0, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

        wr.vertex(posMat, 0, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, 0, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, 0, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, 0, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

        wr.vertex(posMat, size, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, size, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, size, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, size, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

        wr.vertex(posMat, 0, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, size, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, size, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, 0, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

        wr.vertex(posMat, 0, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, size, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, size, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        wr.vertex(posMat, 0, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
    }
}
