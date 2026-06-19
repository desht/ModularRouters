package me.desht.modularrouters.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.render.ModRenderTypes;
import me.desht.modularrouters.client.render.blockentity.ModularRouterRenderState.RenderedBeamData;
import me.desht.modularrouters.client.util.BoxVertices;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import static me.desht.modularrouters.client.util.BoxVertices.BOX_START;
import static me.desht.modularrouters.client.util.BoxVertices.SHAPE;

public class ModularRouterBER implements BlockEntityRenderer<ModularRouterBlockEntity, ModularRouterRenderState> {
    private static final Vector3f ROTATION = new Vector3f(0.15f, 1.0f, 0f);

    private final ItemModelResolver itemModelResolver;

    @SuppressWarnings("unused")
    public ModularRouterBER(BlockEntityRendererProvider.Context ctx) {
        itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public AABB getRenderBoundingBox(ModularRouterBlockEntity blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }

    @Override
    public ModularRouterRenderState createRenderState() {
        return new ModularRouterRenderState();
    }

    @Override
    public void extractRenderState(ModularRouterBlockEntity blockEntity, ModularRouterRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.beams = blockEntity.beams.stream()
                .map(beamData -> RenderedBeamData.create(beamData, blockEntity.getLevel(), partialTick, itemModelResolver))
                .toList();
        renderState.camouflage = blockEntity.getCamouflage();
    }

    @Override
    public void submit(ModularRouterRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        Vec3 routerVec = Vec3.atCenterOf(renderState.blockPos);
        for (RenderedBeamData beam : renderState.beams) {
            poseStack.pushPose();
            poseStack.translate(-routerVec.x(), -routerVec.y(), -routerVec.z());
            Vec3 startPos = beam.beamData().getStart(routerVec);
            Vec3 endPos = beam.beamData().getEnd(routerVec);
            float progress = beam.progress();
            if (ConfigHolder.client.misc.renderFlyingItems.get()) {
                renderFlyingItem(beam.beamData(), beam.renderState(), poseStack, submitNodeCollector, progress, startPos, endPos);
            }
            renderBeamLine(beam.beamData(), poseStack, submitNodeCollector, progress, startPos, endPos);
            poseStack.popPose();
        }
        poseStack.popPose();

        Player player = Minecraft.getInstance().player;
        if (ConfigHolder.client.misc.heldRouterShowsCamoRouters.get()
                && renderState.camouflage != null
                && playerHoldingMRItem(player)
                && Vec3.atCenterOf(renderState.blockPos).distanceToSqr(player.position()) < 256)
        {
            poseStack.pushPose();
            submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.BLOCK_HILIGHT_FACE, this::drawFaces);
            submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.BLOCK_HILIGHT_LINE, this::drawLines);
            poseStack.popPose();
        }
    }

    private void drawFaces(PoseStack.Pose pose, VertexConsumer vc) {
        pose.translate(BOX_START, BOX_START, BOX_START);
        BoxVertices.FACE_VERTICES.forEach((dir, vertices) -> {
            for (float[] vertex : vertices) {
                vc.addVertex(pose, vertex[0], vertex[1], vertex[2]).setColor(0x60808080).setNormal(pose, dir.getUnitVec3f());
            }
        });
    }

    private void drawLines(PoseStack.Pose pose, VertexConsumer vc) {
        float lineWidth = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth * 3f;
        SHAPE.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            Vector3f normal = new Vector3f((float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1)).normalize();
            vc.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(0xFF600000).setNormal(pose, normal).setLineWidth(lineWidth);
            vc.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(0xFF600000).setNormal(pose, normal).setLineWidth(lineWidth);
        });
    }

    private static boolean playerHoldingMRItem(@Nullable Player player) {
        return player != null && BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).getNamespace().equals(ModularRouters.MODID);
    }

    private void renderFlyingItem(BeamData beam, ItemStackRenderState itemStackRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float progress, Vec3 startPos, Vec3 endPos) {
        double ix = Mth.lerp(progress, startPos.x(), endPos.x());
        double iy = Mth.lerp(progress, startPos.y(), endPos.y());
        double iz = Mth.lerp(progress, startPos.z(), endPos.z());
        BlockPos pos = BlockPos.containing(ix, iy, iz);
        Level level = Minecraft.getInstance().level;
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        if (shape.isEmpty() || !shape.bounds().move(pos).contains(ix, iy, iz)) {
            poseStack.pushPose();
            poseStack.translate(ix, iy - 0.15, iz);
            poseStack.mulPose(Axis.of(ROTATION).rotationDegrees(progress * 360));
            if (beam.fade()) {
                poseStack.translate(0, 0.15, 0);
                poseStack.scale(1.15f - progress, 1.15f - progress, 1.15f - progress);
                RandomSource random = level.getRandom();
                if (progress > 0.95 && random.nextInt(3) == 0) {
                    level.addParticle(ParticleTypes.PORTAL, endPos.x(), endPos.y(), endPos.z(), 0.5 - random.nextDouble(), -0.5, 0.5 - random.nextDouble());
                }
            }

            itemStackRenderState.submit(poseStack, submitNodeCollector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);

            poseStack.popPose();
        }
    }

    private void renderBeamLine(BeamData beam, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, float progress, Vec3 startPos, Vec3 endPos) {
        int alpha = (int) (Mth.sin((Minecraft.getInstance().level.getGameTime() % 20) / 20f * 3.1415927f) * 128 + 32);
        int[] colors = beam.getRGB();
        Matrix4f positionMatrix = matrixStack.last().pose();
        double len = startPos.distanceTo(endPos);
        float xn = (float) ((endPos.x - startPos.x) / len);
        float yn = (float) ((endPos.y - startPos.y) / len);
        float zn = (float) ((endPos.z - startPos.z) / len);

        submitNodeCollector.submitCustomGeometry(matrixStack, ModRenderTypes.BEAM_LINE, (pose, buffer) -> {
            ClientUtil.posF(buffer, positionMatrix, startPos, ModRenderTypes.THICK_LINE)
                    .setColor(colors[0], colors[1], colors[2], alpha)
                    .setNormal(pose, xn, yn, zn);
            ClientUtil.posF(buffer, positionMatrix, endPos, ModRenderTypes.THICK_LINE)
                    .setColor(colors[0], colors[1], colors[2], alpha)
                    .setNormal(pose, xn, yn, zn);
        });

        submitNodeCollector.submitCustomGeometry(matrixStack, ModRenderTypes.BEAM_LINE, (pose, buffer) -> {
            ClientUtil.posF(buffer, positionMatrix, startPos, ModRenderTypes.THIN_LINE)
                    .setColor(colors[0], colors[1], colors[2], 192)
                    .setNormal(pose, xn, yn, zn);
            ClientUtil.posF(buffer, positionMatrix, endPos, ModRenderTypes.THIN_LINE)
                    .setColor(colors[0], colors[1], colors[2], 192)
                    .setNormal(pose, xn, yn, zn);
        });
    }

}
