package me.desht.modularrouters.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.render.ModRenderTypes;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public class ModularRouterBER implements BlockEntityRenderer<ModularRouterBlockEntity, ModularRouterRenderState> {
    private static final Vector3f ROTATION = new Vector3f(0.15f, 1.0f, 0f);
    private static final float CAMO_HIGHLIGHT_SIZE = 0.75f;

    private static final VoxelShape CAMO_HIGHLIGHT_SHAPE = Shapes.box(
            0, 0, 0, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE
    );

    private static final float[] COLS = new float[] { 0.5f, 0.5f, 1.0f, 0.25f };

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
        renderState.beams = blockEntity.beams.stream().map(e -> new BeamData.WithProgress(e, e.getProgress(partialTick))).toList();
        renderState.beamItems = new ArrayList<>(blockEntity.beams.size());
        blockEntity.beams.forEach(beam -> {
            ItemStackRenderState state = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(state, beam.stack(), ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
            renderState.beamItems.add(state);
        });
        renderState.camouflage = blockEntity.getCamouflage();
    }

    @Override
    public void submit(ModularRouterRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        Vec3 routerVec = Vec3.atCenterOf(renderState.blockPos);
        for (int i = 0; i < renderState.beams.size(); i++) {
            BeamData.WithProgress beam = renderState.beams.get(i);
            poseStack.pushPose();
            poseStack.translate(-routerVec.x(), -routerVec.y(), -routerVec.z());
            Vec3 startPos = beam.beam().getStart(routerVec);
            Vec3 endPos = beam.beam().getEnd(routerVec);
            float progress = beam.progress();
            if (ConfigHolder.client.misc.renderFlyingItems.get()) {
                renderFlyingItem(beam.beam(), renderState.beamItems.get(i), poseStack, submitNodeCollector, progress, startPos, endPos);
            }
            renderBeamLine(beam.beam(), poseStack, submitNodeCollector, progress, startPos, endPos);
            poseStack.popPose();
        }
        poseStack.popPose();

        Player player = Minecraft.getInstance().player;
        if (ConfigHolder.client.misc.heldRouterShowsCamoRouters.get()
                && renderState.camouflage != null
                && playerHoldingRouter(player)
                && Vec3.atCenterOf(renderState.blockPos).distanceToSqr(player.position()) < 256) {
            renderCamoHighlight(poseStack, submitNodeCollector);
        }
    }

    private void renderCamoHighlight(PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        double start = (1 - CAMO_HIGHLIGHT_SIZE) / 2.0;
        poseStack.translate(start, start, start);

        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.BLOCK_HILIGHT_FACE, (pose, buffer) -> {
            addVertices(buffer, pose.pose());
        });

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.secondaryBlockOutline(), (pose, buffer) -> {
            ShapeRenderer.renderShape(
                    poseStack,
                    buffer,
                    CAMO_HIGHLIGHT_SHAPE,
                    0, 0, 0,
                    ARGB.colorFromFloat(1.0F, 0.5F, 0.5F, 1.0F),
                    3.0f // Line width
            );
        });

        poseStack.popPose();
    }

    private void addVertices(VertexConsumer wr, Matrix4f posMat) {
        wr.addVertex(posMat, 0, 0, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 0, -1);
        wr.addVertex(posMat, 0, CAMO_HIGHLIGHT_SIZE, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 0, -1);
        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 0, -1);
        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, 0, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 0, -1);

        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, 0, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 0, 1);
        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 0, 1);
        wr.addVertex(posMat, 0, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 0, 1);
        wr.addVertex(posMat, 0, 0, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 0, 1);

        wr.addVertex(posMat, 0, 0, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(-1, 0, 0);
        wr.addVertex(posMat, 0, 0, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(-1, 0, 0);
        wr.addVertex(posMat, 0, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(-1, 0, 0);
        wr.addVertex(posMat, 0, CAMO_HIGHLIGHT_SIZE, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(-1, 0, 0);

        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(1, 0, 0);
        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(1, 0, 0);
        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, 0, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(1, 0, 0);
        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, 0, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(1, 0, 0);

        wr.addVertex(posMat, 0, 0, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, -1, 0);
        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, 0, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, -1, 0);
        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, 0, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, -1, 0);
        wr.addVertex(posMat, 0, 0, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, -1, 0);

        wr.addVertex(posMat, 0, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 1, 0);
        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 1, 0);
        wr.addVertex(posMat, CAMO_HIGHLIGHT_SIZE, CAMO_HIGHLIGHT_SIZE, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 1, 0);
        wr.addVertex(posMat, 0, CAMO_HIGHLIGHT_SIZE, 0).setColor(COLS[0], COLS[1], COLS[2], COLS[3]).setNormal(0, 1, 0);
    }

    private static boolean playerHoldingRouter(Player player) {
        Item router = ModBlocks.MODULAR_ROUTER.get().asItem();
        return player.getMainHandItem().getItem() == router || player.getOffhandItem().getItem() == router;
    }

    private void renderFlyingItem(BeamData beam, ItemStackRenderState itemStackRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float progress, Vec3 startPos, Vec3 endPos) {
        double ix = Mth.lerp(progress, startPos.x(), endPos.x());
        double iy = Mth.lerp(progress, startPos.y(), endPos.y());
        double iz = Mth.lerp(progress, startPos.z(), endPos.z());
        BlockPos pos = BlockPos.containing(ix, iy, iz);
        Level world = Minecraft.getInstance().level;
        VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
        if (shape.isEmpty() || !shape.bounds().move(pos).contains(ix, iy, iz)) {
            poseStack.pushPose();
            poseStack.translate(ix, iy - 0.15, iz);
            poseStack.mulPose(Axis.of(ROTATION).rotationDegrees(progress * 360));
            if (beam.fade()) {
                poseStack.translate(0, 0.15, 0);
                poseStack.scale(1.15f - progress, 1.15f - progress, 1.15f - progress);
                if (progress > 0.95 && world.random.nextInt(3) == 0) {
                    world.addParticle(ParticleTypes.PORTAL, endPos.x(), endPos.y(), endPos.z(), 0.5 - world.random.nextDouble(), -0.5, 0.5 - world.random.nextDouble());
                }
            }

            itemStackRenderState.submit(poseStack, submitNodeCollector, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);

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
