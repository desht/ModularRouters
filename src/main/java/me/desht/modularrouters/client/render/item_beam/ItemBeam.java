package me.desht.modularrouters.client.render.item_beam;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.modularrouters.client.render.ModRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemBeam {
    private final Vector3f startPos;
    private final Vector3f endPos;
    private final ItemStack renderItem;
    private final int[] colors;
    private final int lifeTime;  // ticks
    private final boolean itemFade;
    private int ticksLived = 0;

    private static final Vector3f ROTATION = new Vector3f(0.15f, 1.0f, 0f);

    public ItemBeam(BlockPos pos1, BlockPos pos2, ItemStack renderItem, int color, int lifeTime, boolean itemFade) {
        this.startPos = new Vector3f(pos1.getX() + 0.5f, pos1.getY() + 0.5f, pos1.getZ() + 0.5f);
        this.endPos = new Vector3f(pos2.getX() + 0.5f, pos2.getY() + 0.5f, pos2.getZ() + 0.5f);
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

    public void render(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, float partialTicks) {
        float f = (ticksLived + partialTicks) / lifeTime;
        int alpha = (int)((1 - Math.abs(f - 0.5)) * 32 + 16);

        Matrix4f positionMatrix = matrixStack.getLast().getMatrix();
        IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.BEAM_LINE_THICK);
        builder.pos(positionMatrix, startPos.getX(), startPos.getY(), startPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha)
                .endVertex();
        builder.pos(positionMatrix, endPos.getX(), endPos.getY(), endPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha)
                .endVertex();
        RenderSystem.disableDepthTest();
        buffer.finish(ModRenderTypes.BEAM_LINE_THICK);

        IVertexBuilder builder2 = buffer.getBuffer(ModRenderTypes.BEAM_LINE_THIN);
        builder2.pos(positionMatrix, startPos.getX(), startPos.getY(), startPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha)
                .endVertex();
        builder2.pos(positionMatrix, endPos.getX(), endPos.getY(), endPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha)
                .endVertex();
        RenderSystem.disableDepthTest();
        buffer.finish(ModRenderTypes.BEAM_LINE_THIN);

        float ix = MathHelper.lerp(f, startPos.getX(), endPos.getX());
        float iy = MathHelper.lerp(f, startPos.getY(), endPos.getY());
        float iz = MathHelper.lerp(f, startPos.getZ(), endPos.getZ());
        float yRot = f * 360;

        BlockPos pos = new BlockPos(ix, iy, iz);
        World w = Minecraft.getInstance().world;
        if (!w.getBlockState(pos).isSolid()) {
            matrixStack.push();
            matrixStack.translate(ix, iy - 0.15, iz);
            matrixStack.rotate(ROTATION.rotationDegrees(yRot));
            if (itemFade) {
                matrixStack.scale(1.25f - f, 1.25f - f, 1.25f - f);
                if (f > 0.9) {
                    w.addParticle(ParticleTypes.PORTAL, endPos.getX(), endPos.getY(), endPos.getZ(), 0.5 - w.rand.nextDouble(), -0.5, 0.5 - w.rand.nextDouble());
                }
            }
            int l = WorldRenderer.getCombinedLight(w, pos);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            itemRenderer.renderItem(renderItem, ItemCameraTransforms.TransformType.GROUND, l, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.pop();
        }
    }

    boolean isExpired() {
        return ticksLived >= lifeTime;
    }
}
