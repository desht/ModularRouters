package me.desht.modularrouters.client.render.area;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.modularrouters.client.render.ModRenderTypes;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class ModuleTargetRenderer {
    private static final float BOX_SIZE = 0.5f;

    private static ItemStack lastStack = ItemStack.EMPTY;
    private static CompiledPosition compiledPos = null;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().player != null) {
            ItemStack curItem = Minecraft.getInstance().player.getMainHandItem();
            if (!ItemStack.matches(curItem, lastStack)) {
                lastStack = curItem.copy();
                IPositionProvider positionProvider = getPositionProvider(curItem);
                if (positionProvider != null) {
                    compiledPos = new CompiledPosition(curItem, positionProvider);
                } else {
                    compiledPos = null;
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderWorldLastEvent(RenderWorldLastEvent event) {
        if (compiledPos != null) {
            IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            MatrixStack matrixStack = event.getMatrixStack();

            matrixStack.pushPose();

            Vector3d projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
            render(buffer, matrixStack, compiledPos);
            matrixStack.popPose();
        }
    }

    private static IPositionProvider getPositionProvider(ItemStack stack) {
        if (stack.getItem() instanceof IPositionProvider) {
            return (IPositionProvider) stack.getItem();
        } else {
            return null;
        }
    }

    private static void render(IRenderTypeBuffer.Impl buffer, MatrixStack matrixStack, ModuleTargetRenderer.CompiledPosition cp) {
        float start = (1 - BOX_SIZE) / 2.0f;

        for (BlockPos pos : cp.getPositions()) {
            matrixStack.pushPose();
            matrixStack.translate(pos.getX() + start, pos.getY() + start, pos.getZ() + start);
            Matrix4f posMat = matrixStack.last().pose();
            int color = cp.getColour(pos);
            int r = (color & 0xFF0000) >> 16;
            int g = (color & 0xFF00) >> 8;
            int b = color & 0xFF;
            int alpha;

            IVertexBuilder faceBuilder = buffer.getBuffer(ModRenderTypes.BLOCK_HILIGHT_FACE);

            alpha = getFaceAlpha(cp, pos, Direction.NORTH);
            faceBuilder.vertex(posMat,0, 0, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, 0, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, BOX_SIZE, 0, 0).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.SOUTH);
            faceBuilder.vertex(posMat, BOX_SIZE, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, 0, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, 0, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.WEST);
            faceBuilder.vertex(posMat, 0, 0, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, 0, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, 0, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, 0, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.EAST);
            faceBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, BOX_SIZE, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, BOX_SIZE, 0, 0).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.DOWN);
            faceBuilder.vertex(posMat, 0, 0, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, BOX_SIZE, 0, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, BOX_SIZE, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, 0, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.UP);
            faceBuilder.vertex(posMat, 0, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.vertex(posMat, 0, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();

            RenderSystem.disableDepthTest();
            buffer.endBatch(ModRenderTypes.BLOCK_HILIGHT_FACE);

            IVertexBuilder lineBuilder = buffer.getBuffer(ModRenderTypes.BLOCK_HILIGHT_LINE);

            lineBuilder.vertex(posMat, 0, 0, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, 0, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, BOX_SIZE, 0, 0).color(64, 64, 64, 80).endVertex();

            lineBuilder.vertex(posMat, BOX_SIZE, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, 0, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, 0, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();

            lineBuilder.vertex(posMat, 0, 0, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, 0, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, 0, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, 0, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();

            lineBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, BOX_SIZE, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, BOX_SIZE, 0, 0).color(64, 64, 64, 80).endVertex();

            lineBuilder.vertex(posMat, 0, 0, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, BOX_SIZE, 0, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, BOX_SIZE, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, 0, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();

            lineBuilder.vertex(posMat, 0, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, BOX_SIZE, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.vertex(posMat, 0, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();

            RenderSystem.disableDepthTest();
            buffer.endBatch(ModRenderTypes.BLOCK_HILIGHT_LINE);

            matrixStack.popPose();
        }
    }

    private static int getFaceAlpha(ModuleTargetRenderer.CompiledPosition cp, BlockPos pos, Direction face) {
        return cp.checkFace(pos, face) ? 160 : 40;
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
                        positions.get(pos).faces.set(target.face.ordinal());
                    } else {
                        FaceAndColour fc = new FaceAndColour(new BitSet(6), provider.getRenderColor(i));
                        fc.faces.set(target.face.ordinal());
                        positions.put(pos, fc);
                    }
                }
            }
        }

        Set<BlockPos> getPositions() {
            return positions.keySet();
        }

        boolean checkFace(BlockPos pos, Direction face) {
            return positions.containsKey(pos) && positions.get(pos).faces.get(face.get3DDataValue());
        }

        int getColour(BlockPos pos) {
            return positions.containsKey(pos) ? positions.get(pos).colour : 0;
        }

        static class FaceAndColour {
            final BitSet faces;
            final int colour;

            FaceAndColour(BitSet faces, int colour) {
                this.faces = faces;
                this.colour = colour;
            }
        }
    }

}
