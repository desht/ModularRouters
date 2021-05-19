package me.desht.modularrouters.client.render.item_beam;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.render.ModRenderTypes;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
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

        Vector3d routerVec = Vector3d.atCenterOf(te.getBlockPos());
        for (BeamData beam: te.beams) {
            matrixStack.pushPose();
            matrixStack.translate(-routerVec.x(), -routerVec.y(), -routerVec.z());
            Vector3d startPos = beam.getStart(routerVec);
            Vector3d endPos = beam.getEnd(routerVec);
            float progress = beam.getProgress(partialTicks);
            if (MRConfig.Client.Misc.renderFlyingItems) {
                renderFlyingItem(beam, matrixStack, buffer, progress, startPos, endPos);
            }
            renderBeamLine(beam, matrixStack, buffer, progress, startPos, endPos);
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    private void renderFlyingItem(BeamData beam, MatrixStack matrixStack, IRenderTypeBuffer buffer, float progress, Vector3d startPos, Vector3d endPos) {
        double ix = MathHelper.lerp(progress, startPos.x(), endPos.x());
        double iy = MathHelper.lerp(progress, startPos.y(), endPos.y());
        double iz = MathHelper.lerp(progress, startPos.z(), endPos.z());
        BlockPos pos = new BlockPos(ix, iy, iz);
        World world = Minecraft.getInstance().level;
        VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
        if (shape.isEmpty() || !shape.bounds().move(pos).contains(ix, iy, iz)) {
            matrixStack.pushPose();
            matrixStack.translate(ix, iy - 0.15, iz);
            matrixStack.mulPose(ROTATION.rotationDegrees(progress * 360));
            if (beam.isItemFade()) {
                matrixStack.translate(0, 0.15, 0);
                matrixStack.scale(1.15f - progress, 1.15f - progress, 1.15f - progress);
                if (progress > 0.95 && world.random.nextInt(3) == 0) {
                    world.addParticle(ParticleTypes.PORTAL, endPos.x(), endPos.y(), endPos.z(), 0.5 - world.random.nextDouble(), -0.5, 0.5 - world.random.nextDouble());
                }
            }
            Minecraft.getInstance().getItemRenderer()
                    .renderStatic(beam.getStack(), TransformType.GROUND, 0x00F000F0, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.popPose();
        }
    }

    private void renderBeamLine(BeamData beam, MatrixStack matrixStack, IRenderTypeBuffer buffer, float progress, Vector3d startPos, Vector3d endPos) {
        int alpha = (int) (MathHelper.sin((Minecraft.getInstance().level.getGameTime() % 20) / 20f * 3.1415927f) * 128 + 32);
        int[] colors = beam.getRGB();
        Matrix4f positionMatrix = matrixStack.last().pose();
        IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.BEAM_LINE_THICK);
        ClientUtil.posF(builder, positionMatrix, startPos)
                .color(colors[0], colors[1], colors[2], alpha)
                .endVertex();
        ClientUtil.posF(builder, positionMatrix, endPos)
                .color(colors[0], colors[1], colors[2], alpha)
                .endVertex();

        IVertexBuilder builder2 = buffer.getBuffer(ModRenderTypes.BEAM_LINE_THIN);
        ClientUtil.posF(builder2, positionMatrix, startPos)
                .color(colors[0], colors[1], colors[2], 192)
                .endVertex();
        ClientUtil.posF(builder2, positionMatrix, endPos)
                .color(colors[0], colors[1], colors[2], 192)
                .endVertex();
    }

}
