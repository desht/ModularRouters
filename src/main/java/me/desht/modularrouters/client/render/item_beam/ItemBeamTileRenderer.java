package me.desht.modularrouters.client.render.item_beam;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.render.ModRenderTypes;
import me.desht.modularrouters.config.MRConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

public class ItemBeamTileRenderer extends TileEntityRenderer<TileEntityItemRouter> {
    private static final Vector3f ROTATION = new Vector3f(0.15f, 1.0f, 0f);

    public ItemBeamTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(TileEntityItemRouter te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {

        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);

        for (ItemBeam beam: te.beams) {
            matrixStack.pushPose();
            if (beam.reversed) {
                matrixStack.translate(-beam.endPos.x(), -beam.endPos.y(), -beam.endPos.z());
            } else {
                matrixStack.translate(-beam.startPos.x(), -beam.startPos.y(), -beam.startPos.z());
            }
            float progress = (beam.ticksLived + partialTicks) / beam.lifeTime;
            if (MRConfig.Client.Misc.renderFlyingItems) {
                renderFlyingItem(beam, matrixStack, buffer, progress);
            }
            renderBeamLine(beam, matrixStack, buffer, progress);
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    private void renderFlyingItem(ItemBeam beam, MatrixStack matrixStack, IRenderTypeBuffer buffer, float progress) {
        float ix = MathHelper.lerp(progress, beam.startPos.x(), beam.endPos.x());
        float iy = MathHelper.lerp(progress, beam.startPos.y(), beam.endPos.y());
        float iz = MathHelper.lerp(progress, beam.startPos.z(), beam.endPos.z());
        BlockPos pos = new BlockPos(ix, iy, iz);
        World world = Minecraft.getInstance().level;
        VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
        if (shape.isEmpty() || !shape.bounds().move(pos).contains(ix, iy, iz)) {
            matrixStack.pushPose();
            matrixStack.translate(ix, iy - 0.15, iz);
            matrixStack.mulPose(ROTATION.rotationDegrees(progress * 360));
            if (beam.itemFade) {
                matrixStack.scale(1.25f - progress, 1.25f - progress, 1.25f - progress);
                if (progress > 0.9) {
                    world.addParticle(ParticleTypes.PORTAL, beam.endPos.x(), beam.endPos.y(), beam.endPos.z(), 0.5 - world.random.nextDouble(), -0.5, 0.5 - world.random.nextDouble());
                }
            }
            int light = WorldRenderer.getLightColor(world, pos);
            Minecraft.getInstance().getItemRenderer()
                    .renderStatic(beam.renderItem, TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.popPose();
        }
    }

    private void renderBeamLine(ItemBeam beam, MatrixStack matrixStack, IRenderTypeBuffer buffer, float progress) {
        int alpha = (int)((1 - Math.abs(progress - 0.5)) * 32 + 16);

        Matrix4f positionMatrix = matrixStack.last().pose();
        IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.BEAM_LINE_THICK);
        builder.vertex(positionMatrix, beam.startPos.x(), beam.startPos.y(), beam.startPos.z())
                .color(beam.colors[0], beam.colors[1], beam.colors[2], alpha)
                .endVertex();
        builder.vertex(positionMatrix, beam.endPos.x(), beam.endPos.y(), beam.endPos.z())
                .color(beam.colors[0], beam.colors[1], beam.colors[2], alpha)
                .endVertex();

        IVertexBuilder builder2 = buffer.getBuffer(ModRenderTypes.BEAM_LINE_THIN);
        builder2.vertex(positionMatrix, beam.startPos.x(), beam.startPos.y(), beam.startPos.z())
                .color(beam.colors[0], beam.colors[1], beam.colors[2], alpha * 2)
                .endVertex();
        builder2.vertex(positionMatrix, beam.endPos.x(), beam.endPos.y(), beam.endPos.z())
                .color(beam.colors[0], beam.colors[1], beam.colors[2], alpha * 2)
                .endVertex();
    }
}
