package me.desht.modularrouters.client.render.item_beam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.render.ModRenderTypes;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ItemBeamTileRenderer implements BlockEntityRenderer<ModularRouterBlockEntity> {
    private static final Vector3f ROTATION = new Vector3f(0.15f, 1.0f, 0f);

    public ItemBeamTileRenderer(BlockEntityRendererProvider.Context ctx) {
//        super(rendererDispatcherIn);
    }

    @Override
    public void render(ModularRouterBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);

        Vec3 routerVec = Vec3.atCenterOf(te.getBlockPos());
        for (BeamData beam: te.beams) {
            matrixStack.pushPose();
            matrixStack.translate(-routerVec.x(), -routerVec.y(), -routerVec.z());
            Vec3 startPos = beam.getStart(routerVec);
            Vec3 endPos = beam.getEnd(routerVec);
            float progress = beam.getProgress(partialTicks);
            if (MRConfig.Client.Misc.renderFlyingItems) {
                renderFlyingItem(beam, matrixStack, buffer, progress, startPos, endPos);
            }
            renderBeamLine(beam, matrixStack, buffer, progress, startPos, endPos);
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    private void renderFlyingItem(BeamData beam, PoseStack matrixStack, MultiBufferSource buffer, float progress, Vec3 startPos, Vec3 endPos) {
        double ix = Mth.lerp(progress, startPos.x(), endPos.x());
        double iy = Mth.lerp(progress, startPos.y(), endPos.y());
        double iz = Mth.lerp(progress, startPos.z(), endPos.z());
        BlockPos pos = new BlockPos(ix, iy, iz);
        Level world = Minecraft.getInstance().level;
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
            // TODO what's the new int param at the end? is 0 ok?
            Minecraft.getInstance().getItemRenderer()
                    .renderStatic(beam.getStack(), TransformType.GROUND, 0x00F000F0, OverlayTexture.NO_OVERLAY, matrixStack, buffer, 0);
            matrixStack.popPose();
        }
    }

    private void renderBeamLine(BeamData beam, PoseStack matrixStack, MultiBufferSource buffer, float progress, Vec3 startPos, Vec3 endPos) {
        int alpha = (int) (Mth.sin((Minecraft.getInstance().level.getGameTime() % 20) / 20f * 3.1415927f) * 128 + 32);
        int[] colors = beam.getRGB();
        Matrix4f positionMatrix = matrixStack.last().pose();
        double len = startPos.distanceTo(endPos);
        float xn = (float) ((endPos.x - startPos.x) / len);
        float yn = (float) ((endPos.y - startPos.y) / len);
        float zn = (float) ((endPos.z - startPos.z) / len);

        VertexConsumer builder = buffer.getBuffer(ModRenderTypes.BEAM_LINE_THICK);
        ClientUtil.posF(builder, positionMatrix, startPos)
                .color(colors[0], colors[1], colors[2], alpha)
                .normal(matrixStack.last().normal(), xn, yn, zn)
                .endVertex();
        ClientUtil.posF(builder, positionMatrix, endPos)
                .color(colors[0], colors[1], colors[2], alpha)
                .normal(matrixStack.last().normal(), xn, yn, zn)
                .endVertex();

        VertexConsumer builder2 = buffer.getBuffer(ModRenderTypes.BEAM_LINE_THIN);
        ClientUtil.posF(builder2, positionMatrix, startPos)
                .color(colors[0], colors[1], colors[2], 192)
                .normal(matrixStack.last().normal(), xn, yn, zn)
                .endVertex();
        ClientUtil.posF(builder2, positionMatrix, endPos)
                .color(colors[0], colors[1], colors[2], 192)
                .normal(matrixStack.last().normal(), xn, yn, zn)
                .endVertex();
    }

}
