package me.desht.modularrouters.client.render;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.util.BoxVertices;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.*;

public class ModuleTargetRenderer {
    private static final ContextKey<ExtractedPositions> KEY = new ContextKey<>(ModularRouters.id("target_renderer"));

    public static void addEventListeners() {
        NeoForge.EVENT_BUS.addListener(ModuleTargetRenderer::extractRenderState);
        NeoForge.EVENT_BUS.addListener(ModuleTargetRenderer::submitGeometry);
    }

    private static void extractRenderState(ExtractLevelRenderStateEvent event) {
        Player player = ClientUtil.getClientPlayer();
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof IPositionProvider posProvider) {
            event.getRenderState().setRenderData(KEY, ExtractedPositions.extract(player, heldItem, posProvider));
        }
    }

    private static void submitGeometry(SubmitCustomGeometryEvent event) {
        var extracted = event.getLevelRenderState().getRenderData(KEY);

        if (extracted != null) {
            Vec3 viewPos = event.getLevelRenderState().cameraRenderState.pos;
            float lineWidth = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
            var poseStack = event.getPoseStack();

            extracted.positions().forEach((pos, facesAndColour) -> {
                poseStack.pushPose();
                poseStack.translate(pos.getX() - viewPos.x, pos.getY() - viewPos.y, pos.getZ() - viewPos.z);

                event.getSubmitNodeCollector().submitSpecial(RenderPhaseKeys.OUTLINE, new CustomFeatureRenderer.Submit(
                        poseStack.last().copy(), ModRenderTypes.BLOCK_HILIGHT_FACE, new FaceRenderer(facesAndColour))
                );

                event.getSubmitNodeCollector().submitSpecial(RenderPhaseKeys.OUTLINE, new CustomFeatureRenderer.Submit(
                        poseStack.last().copy(), ModRenderTypes.BLOCK_HILIGHT_LINE, new LineRenderer(lineWidth, facesAndColour.color()))
                );

                poseStack.popPose();
            });
        }
    }

    private record FaceRenderer(FacesAndColour facesAndColour) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose pose, VertexConsumer vc) {
            pose.translate(BoxVertices.BOX_START, BoxVertices.BOX_START, BoxVertices.BOX_START);

            BoxVertices.FACE_VERTICES.forEach((dir, vertices) -> {
                int col = facesAndColour.effectiveColor(dir);
                for (float[] vertex : vertices) {
                    vc.addVertex(pose, vertex[0], vertex[1], vertex[2]).setColor(col).setNormal(pose, dir.getUnitVec3f());
                }
            });
        }
    }

    private record LineRenderer(float lineWidth, int color) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose pose, VertexConsumer vc) {
            BoxVertices.SHAPE.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
                Vector3f normal = (new Vector3f((float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1))).normalize();
                vc.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(color).setNormal(pose, normal).setLineWidth(lineWidth);
                vc.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(color).setNormal(pose, normal).setLineWidth(lineWidth);
            });
        }
    }

    private record ExtractedPositions(Map<BlockPos, FacesAndColour> positions) {
        static ExtractedPositions extract(Player player, ItemStack stack, IPositionProvider provider) {
            Map<BlockPos, FacesAndColour> newMap = new HashMap<>();

            List<ModuleTarget> targets = provider.getStoredPositions(stack);
            for (int i = 0; i < targets.size(); i++) {
                ModuleTarget target = targets.get(i);
                BlockPos pos = target.gPos.pos();
                if (target.isSameWorld(player.level()) && player.level().isLoaded(pos)) {
                    var fc = newMap.get(pos);
                    if (fc != null) {
                        fc.addFace(target.face) ;
                    } else {
                        newMap.put(pos, FacesAndColour.create(target.face, provider.getRenderColor(i)));
                    }
                }
            }

            ImmutableMap.Builder<BlockPos, FacesAndColour> builder = ImmutableMap.builder();
            newMap.forEach((pos, fc) -> builder.put(pos, fc.toImmutable()));
            return new ExtractedPositions(builder.build());
        }
    }

    private record FacesAndColour(Set<Direction> faces, int color, int fadeColor) {
        static FacesAndColour create(Direction dir, int color) {
            return new FacesAndColour(EnumSet.of(dir), color, ARGB.multiplyAlpha(color, 0.2f));
        }

        void addFace(Direction dir) {
            faces.add(dir);
        }

        public FacesAndColour toImmutable() {
            return new FacesAndColour(Sets.immutableEnumSet(faces), color, fadeColor);
        }

        public int effectiveColor(Direction dir) {
            return faces.contains(dir) ? color : fadeColor;
        }
    }
}
