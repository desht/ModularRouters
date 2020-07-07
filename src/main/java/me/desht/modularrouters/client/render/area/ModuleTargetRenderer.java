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
            ItemStack curItem = Minecraft.getInstance().player.getHeldItemMainhand();
            if (!ItemStack.areItemStacksEqual(curItem, lastStack)) {
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
            IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            MatrixStack matrixStack = event.getMatrixStack();

            matrixStack.push();

            Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
            matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
            render(buffer, matrixStack, compiledPos);
            matrixStack.pop();
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
            matrixStack.push();
            matrixStack.translate(pos.getX() + start, pos.getY() + start, pos.getZ() + start);
            Matrix4f posMat = matrixStack.getLast().getMatrix();
            int color = cp.getColour(pos);
            int r = (color & 0xFF0000) >> 16;
            int g = (color & 0xFF00) >> 8;
            int b = color & 0xFF;
            int alpha;

            IVertexBuilder faceBuilder = buffer.getBuffer(ModRenderTypes.BLOCK_HILIGHT_FACE);

            alpha = getFaceAlpha(cp, pos, Direction.NORTH);
            faceBuilder.pos(posMat,0, 0, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, 0, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, BOX_SIZE, 0, 0).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.SOUTH);
            faceBuilder.pos(posMat, BOX_SIZE, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, 0, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, 0, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.WEST);
            faceBuilder.pos(posMat, 0, 0, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, 0, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, 0, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, 0, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.EAST);
            faceBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, BOX_SIZE, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, BOX_SIZE, 0, 0).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.DOWN);
            faceBuilder.pos(posMat, 0, 0, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, BOX_SIZE, 0, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, BOX_SIZE, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, 0, 0, BOX_SIZE).color(r, g, b, alpha).endVertex();

            alpha = getFaceAlpha(cp, pos, Direction.UP);
            faceBuilder.pos(posMat, 0, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();
            faceBuilder.pos(posMat, 0, BOX_SIZE, 0).color(r, g, b, alpha).endVertex();

//            RenderSystem.disableDepthTest();
            buffer.finish(ModRenderTypes.BLOCK_HILIGHT_FACE);

            IVertexBuilder lineBuilder = buffer.getBuffer(ModRenderTypes.BLOCK_HILIGHT_LINE);

            lineBuilder.pos(posMat, 0, 0, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, 0, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, BOX_SIZE, 0, 0).color(64, 64, 64, 80).endVertex();

            lineBuilder.pos(posMat, BOX_SIZE, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, 0, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, 0, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();

            lineBuilder.pos(posMat, 0, 0, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, 0, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, 0, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, 0, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();

            lineBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, BOX_SIZE, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, BOX_SIZE, 0, 0).color(64, 64, 64, 80).endVertex();

            lineBuilder.pos(posMat, 0, 0, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, BOX_SIZE, 0, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, BOX_SIZE, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, 0, 0, BOX_SIZE).color(64, 64, 64, 80).endVertex();

            lineBuilder.pos(posMat, 0, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, BOX_SIZE).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, BOX_SIZE, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();
            lineBuilder.pos(posMat, 0, BOX_SIZE, 0).color(64, 64, 64, 80).endVertex();

//            RenderSystem.disableDepthTest();
            buffer.finish(ModRenderTypes.BLOCK_HILIGHT_LINE);

            matrixStack.pop();
        }
    }

    private static int getFaceAlpha(ModuleTargetRenderer.CompiledPosition cp, BlockPos pos, Direction face) {
        return cp.checkFace(pos, face) ? 160 : 64;
    }

    static class CompiledPosition {
        Map<BlockPos, FaceAndColour> positions = new HashMap<>();

        CompiledPosition(ItemStack stack, IPositionProvider provider) {
            List<ModuleTarget> targets = provider.getStoredPositions(stack);
            for (int i = 0; i < targets.size(); i++) {
                ModuleTarget target = targets.get(i);
                if (target.isSameWorld(Minecraft.getInstance().world)) {
                    BlockPos pos = target.gPos.getPos();
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
            return positions.containsKey(pos) && positions.get(pos).faces.get(face.getIndex());
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
