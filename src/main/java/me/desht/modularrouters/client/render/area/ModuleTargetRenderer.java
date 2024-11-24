package me.desht.modularrouters.client.render.area;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.modularrouters.client.render.ModRenderTypes;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleTargetRenderer {
    private static final float BOX_SIZE = 0.5f;
    private static final float BOX_START = (1f - BOX_SIZE) / 2f;

    private static ItemStack lastStack = ItemStack.EMPTY;
    private static CompiledPosition compiledPos = null;

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Pre event) {
        if (Minecraft.getInstance().player != null) {
            ItemStack curItem = Minecraft.getInstance().player.getMainHandItem();
            if (curItem.getItem() instanceof IPositionProvider posProvider) {
                if (!ItemStack.matches(curItem, lastStack)) {
                    lastStack = curItem.copy();
                    compiledPos = new CompiledPosition(curItem, posProvider);
                }
            } else {
                lastStack = ItemStack.EMPTY;
                compiledPos = null;
            }
        }
    }

    @SubscribeEvent
    public static void renderWorldLastEvent(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES && compiledPos != null) {
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            PoseStack matrixStack = event.getPoseStack();

            matrixStack.pushPose();

            Vec3 projectedView = event.getCamera().getPosition();
            matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
            render(buffer, matrixStack, compiledPos);

            matrixStack.popPose();
        }
    }

    private static void render(MultiBufferSource.BufferSource buffer, PoseStack matrixStack, ModuleTargetRenderer.CompiledPosition cp) {
        cp.positions.forEach((pos, faceAndColour) -> {
            matrixStack.pushPose();
            matrixStack.translate(pos.getX() + BOX_START, pos.getY() + BOX_START, pos.getZ() + BOX_START);
            Matrix4f posMat = matrixStack.last().pose();
            int color = faceAndColour.colour();
            int r = (color & 0xFF0000) >> 16;
            int g = (color & 0xFF00) >> 8;
            int b = color & 0xFF;
            int alpha;

            VertexConsumer faceBuilder = buffer.getBuffer(ModRenderTypes.BLOCK_HILIGHT_FACE);

            alpha = getFaceAlpha(faceAndColour, Direction.NORTH);
            faceBuilder.addVertex(posMat,0, 0, 0).setColor(r, g, b, alpha).setNormal(0, 0, -1);
            faceBuilder.addVertex(posMat, 0, BOX_SIZE, 0).setColor(r, g, b, alpha).setNormal(0, 0, -1);
            faceBuilder.addVertex(posMat, BOX_SIZE, BOX_SIZE, 0).setColor(r, g, b, alpha).setNormal(0, 0, -1);
            faceBuilder.addVertex(posMat, BOX_SIZE, 0, 0).setColor(r, g, b, alpha).setNormal(0, 0, -1);

            alpha = getFaceAlpha(faceAndColour, Direction.SOUTH);
            faceBuilder.addVertex(posMat, BOX_SIZE, 0, BOX_SIZE).setColor(r, g, b, alpha).setNormal(0, 0, 1);
            faceBuilder.addVertex(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).setColor(r, g, b, alpha).setNormal(0, 0, 1);
            faceBuilder.addVertex(posMat, 0, BOX_SIZE, BOX_SIZE).setColor(r, g, b, alpha).setNormal(0, 0, 1);
            faceBuilder.addVertex(posMat, 0, 0, BOX_SIZE).setColor(r, g, b, alpha).setNormal(0, 0, 1);

            alpha = getFaceAlpha(faceAndColour, Direction.WEST);
            faceBuilder.addVertex(posMat, 0, 0, 0).setColor(r, g, b, alpha).setNormal(-1, 0, 0);
            faceBuilder.addVertex(posMat, 0, 0, BOX_SIZE).setColor(r, g, b, alpha).setNormal(-1, 0, 0);
            faceBuilder.addVertex(posMat, 0, BOX_SIZE, BOX_SIZE).setColor(r, g, b, alpha).setNormal(-1, 0, 0);
            faceBuilder.addVertex(posMat, 0, BOX_SIZE, 0).setColor(r, g, b, alpha).setNormal(-1, 0, 0);

            alpha = getFaceAlpha(faceAndColour, Direction.EAST);
            faceBuilder.addVertex(posMat, BOX_SIZE, BOX_SIZE, 0).setColor(r, g, b, alpha).setNormal(1, 0, 0);
            faceBuilder.addVertex(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).setColor(r, g, b, alpha).setNormal(1, 0, 0);
            faceBuilder.addVertex(posMat, BOX_SIZE, 0, BOX_SIZE).setColor(r, g, b, alpha).setNormal(1, 0, 0);
            faceBuilder.addVertex(posMat, BOX_SIZE, 0, 0).setColor(r, g, b, alpha).setNormal(1, 0, 0);

            alpha = getFaceAlpha(faceAndColour, Direction.DOWN);
            faceBuilder.addVertex(posMat, 0, 0, 0).setColor(r, g, b, alpha).setNormal(0, -1, 0);
            faceBuilder.addVertex(posMat, BOX_SIZE, 0, 0).setColor(r, g, b, alpha).setNormal(0, -1, 0);
            faceBuilder.addVertex(posMat, BOX_SIZE, 0, BOX_SIZE).setColor(r, g, b, alpha).setNormal(0, -1, 0);
            faceBuilder.addVertex(posMat, 0, 0, BOX_SIZE).setColor(r, g, b, alpha).setNormal(0, -1, 0);

            alpha = getFaceAlpha(faceAndColour, Direction.UP);
            faceBuilder.addVertex(posMat, 0, BOX_SIZE, BOX_SIZE).setColor(r, g, b, alpha).setNormal(0, 1, 0);
            faceBuilder.addVertex(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).setColor(r, g, b, alpha).setNormal(0, 1, 0);
            faceBuilder.addVertex(posMat, BOX_SIZE, BOX_SIZE, 0).setColor(r, g, b, alpha).setNormal(0, 1, 0);
            faceBuilder.addVertex(posMat, 0, BOX_SIZE, 0).setColor(r, g, b, alpha).setNormal(0, 1, 0);

            RenderSystem.disableDepthTest();
            buffer.endBatch(ModRenderTypes.BLOCK_HILIGHT_FACE);

            VertexConsumer lineBuilder = buffer.getBuffer(ModRenderTypes.BLOCK_HILIGHT_LINE);
            ShapeRenderer.renderLineBox(matrixStack, lineBuilder, 0, 0, 0, BOX_SIZE, BOX_SIZE, BOX_SIZE, 0.25f, 0.25f, 0.25f, 0.3125f);

            RenderSystem.disableDepthTest();
            buffer.endBatch(ModRenderTypes.BLOCK_HILIGHT_LINE);

            matrixStack.popPose();
        });
    }


    private static int getFaceAlpha(CompiledPosition.FaceAndColour fc, Direction face) {
        return fc.faces.get(face.get3DDataValue()) ? 160 : 40;
    }

    static class CompiledPosition {
        private final Map<BlockPos, FaceAndColour> positions = new HashMap<>();

        CompiledPosition(ItemStack stack, IPositionProvider provider) {
            List<ModuleTarget> targets = provider.getStoredPositions(stack);
            for (int i = 0; i < targets.size(); i++) {
                ModuleTarget target = targets.get(i);
                if (target.isSameWorld(Minecraft.getInstance().level)) {
                    BlockPos pos = target.gPos.pos();
                    if (positions.containsKey(pos)) {
                        positions.get(pos).faces.set(target.face.get3DDataValue());
                    } else {
                        FaceAndColour fc = new FaceAndColour(new BitSet(6), provider.getRenderColor(i));
                        fc.faces.set(target.face.get3DDataValue());
                        positions.put(pos, fc);
                    }
                }
            }
        }

        record FaceAndColour(BitSet faces, int colour) {
        }
    }

}
