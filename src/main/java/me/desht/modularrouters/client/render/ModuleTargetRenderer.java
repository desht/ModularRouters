package me.desht.modularrouters.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ModuleTargetRenderer {
    private static final float BOX_SIZE = 0.5f;
    private static final float BOX_START = (1f - BOX_SIZE) / 2f;
    private static final VoxelShape BOX_SHAPE = Block.box(
            BOX_START * 16, BOX_START * 16, BOX_START * 16,
            16 - BOX_START * 16, 16 - BOX_START * 16, 16 - BOX_START * 16
    );
    private static final ContextKey<CompiledPosition> KEY = new ContextKey<>(ModularRouters.id("target_renderer"));

    private static ItemStack prevStack = ItemStack.EMPTY;
    @Nullable
    private static CompiledPosition cachedCompilePos = null;

    public static void addEventListeners() {
        NeoForge.EVENT_BUS.addListener(ModuleTargetRenderer::onClientTick);
        NeoForge.EVENT_BUS.addListener(ModuleTargetRenderer::extractRenderState);
        NeoForge.EVENT_BUS.addListener(ModuleTargetRenderer::submitGeometry);
    }

    private static void onClientTick(ClientTickEvent.Pre ignoredEvent) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof IPositionProvider posProvider) {
                if (!ItemStack.matches(heldItem, prevStack)) {
                    prevStack = heldItem.copy();
                    cachedCompilePos = new CompiledPosition(player.level(), heldItem, posProvider);
                }
            } else {
                prevStack = ItemStack.EMPTY;
                cachedCompilePos = null;
            }
        }
    }

    private static void extractRenderState(ExtractLevelRenderStateEvent event) {
        if (cachedCompilePos != null) {
            event.getRenderState().setRenderData(KEY, cachedCompilePos);
        }
    }

    private static void submitGeometry(SubmitCustomGeometryEvent event) {
        var compiledPos = event.getLevelRenderState().getRenderData(KEY);

        if (compiledPos != null) {
            Vec3 viewPosition = event.getLevelRenderState().cameraRenderState.pos;
            float lineWidth = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
            var poseStack = event.getPoseStack();

            compiledPos.positions.forEach((pos, faceAndColour) -> {
                poseStack.pushPose();
                poseStack.translate(pos.getX() - viewPosition.x, pos.getY() - viewPosition.y, pos.getZ() - viewPosition.z);

                event.getSubmitNodeCollector().submitSpecial(RenderPhaseKeys.OUTLINE, new CustomFeatureRenderer.Submit(
                        poseStack.last().copy(), ModRenderTypes.BLOCK_HILIGHT_FACE, new FaceRenderer(faceAndColour))
                );

                event.getSubmitNodeCollector().submitSpecial(RenderPhaseKeys.OUTLINE, new CustomFeatureRenderer.Submit(
                        poseStack.last().copy(), ModRenderTypes.BLOCK_HILIGHT_LINE, new LineRenderer(lineWidth, faceAndColour.colour))
                );

                poseStack.popPose();
            });
        }
    }

    private record FaceRenderer(CompiledPosition.FaceAndColour faceAndColour) implements SubmitNodeCollector.CustomGeometryRenderer {

        private static final Map<Direction, float[][]> VERTICES = Util.make(new EnumMap<>(Direction.class), map -> {
            map.put(Direction.DOWN, new float[][]{{0, 0, 0}, {BOX_SIZE, 0, 0}, {BOX_SIZE, 0, BOX_SIZE}, {0, 0, BOX_SIZE}});
            map.put(Direction.UP, new float[][]{{0, BOX_SIZE, BOX_SIZE}, {BOX_SIZE, BOX_SIZE, BOX_SIZE}, {BOX_SIZE, BOX_SIZE, 0}, {0, BOX_SIZE, 0}});
            map.put(Direction.NORTH, new float[][]{{0, 0, 0}, {0, BOX_SIZE, 0}, {BOX_SIZE, BOX_SIZE, 0}, {BOX_SIZE, 0, 0}});
            map.put(Direction.SOUTH, new float[][]{{BOX_SIZE, 0, BOX_SIZE}, {BOX_SIZE, BOX_SIZE, BOX_SIZE}, {0, BOX_SIZE, BOX_SIZE}, {0, 0, BOX_SIZE}});
            map.put(Direction.WEST, new float[][]{{0, 0, 0}, {0, 0, BOX_SIZE}, {0, BOX_SIZE, BOX_SIZE}, {0, BOX_SIZE, 0}});
            map.put(Direction.EAST, new float[][]{{BOX_SIZE, BOX_SIZE, 0}, {BOX_SIZE, BOX_SIZE, BOX_SIZE}, {BOX_SIZE, 0, BOX_SIZE}, {BOX_SIZE, 0, 0}});
        });

        @Override
        public void render(PoseStack.Pose pose, VertexConsumer vc) {
            pose.translate(BOX_START, BOX_START, BOX_START);

            VERTICES.forEach((dir, vertices) -> {
                int alpha = faceAndColour.faces.contains(dir) ? 0x80 : 0x28;
                int color = faceAndColour.colour & 0x00FFFFFF | alpha << 24;
                for (float[] vertex : vertices) {
                    vc.addVertex(pose, vertex[0], vertex[1], vertex[2]).setColor(color).setNormal(pose, dir.getUnitVec3f());
                }
            });
        }
    }

    private record LineRenderer(float lineWidth, int color) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose pose, VertexConsumer vc) {
            BOX_SHAPE.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
                Vector3f normal = (new Vector3f((float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1))).normalize();
                vc.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(color).setNormal(pose, normal).setLineWidth(lineWidth);
                vc.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(color).setNormal(pose, normal).setLineWidth(lineWidth);
            });
        }
    }

    private static class CompiledPosition {
        private final Map<BlockPos, FaceAndColour> positions = new HashMap<>();

        CompiledPosition(Level level, ItemStack stack, IPositionProvider provider) {
            List<ModuleTarget> targets = provider.getStoredPositions(stack);
            for (int i = 0; i < targets.size(); i++) {
                ModuleTarget target = targets.get(i);
                if (target.isSameWorld(level)) {
                    BlockPos pos = target.gPos.pos();
                    if (positions.containsKey(pos)) {
                        positions.get(pos).addFace(target.face);
                    } else {
                        positions.put(pos, FaceAndColour.create(target.face, provider.getRenderColor(i)));
                    }
                }
            }
        }

        record FaceAndColour(Set<Direction> faces, int colour) {
            static FaceAndColour create(Direction dir, int color) {
                return new FaceAndColour(EnumSet.of(dir), color);
            }

            void addFace(Direction dir) {
                faces.add(dir);
            }
        }
    }
}
