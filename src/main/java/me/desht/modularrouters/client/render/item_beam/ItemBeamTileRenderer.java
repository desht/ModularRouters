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

        matrixStack.push();
        matrixStack.translate(0.5, 0.5, 0.5);

        for (ItemBeam beam: te.beams) {
            matrixStack.push();
            if (beam.reversed) {
                matrixStack.translate(-beam.endPos.getX(), -beam.endPos.getY(), -beam.endPos.getZ());
            } else {
                matrixStack.translate(-beam.startPos.getX(), -beam.startPos.getY(), -beam.startPos.getZ());
            }
            float progress = (beam.ticksLived + partialTicks) / beam.lifeTime;
            if (MRConfig.Client.Misc.renderFlyingItems) {
                renderFlyingItem(beam, matrixStack, buffer, progress);
            }
            renderBeamLine(beam, matrixStack, buffer, progress);
            matrixStack.pop();
        }

        matrixStack.pop();
    }

    private void renderFlyingItem(ItemBeam beam, MatrixStack matrixStack, IRenderTypeBuffer buffer, float progress) {
        float ix = MathHelper.lerp(progress, beam.startPos.getX(), beam.endPos.getX());
        float iy = MathHelper.lerp(progress, beam.startPos.getY(), beam.endPos.getY());
        float iz = MathHelper.lerp(progress, beam.startPos.getZ(), beam.endPos.getZ());
        BlockPos pos = new BlockPos(ix, iy, iz);
        World world = Minecraft.getInstance().world;
        VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
        if (shape.isEmpty() || !shape.getBoundingBox().offset(pos).contains(ix, iy, iz)) {
            matrixStack.push();
            matrixStack.translate(ix, iy - 0.15, iz);
            matrixStack.rotate(ROTATION.rotationDegrees(progress * 360));
            if (beam.itemFade) {
                matrixStack.scale(1.25f - progress, 1.25f - progress, 1.25f - progress);
                if (progress > 0.9) {
                    world.addParticle(ParticleTypes.PORTAL, beam.endPos.getX(), beam.endPos.getY(), beam.endPos.getZ(), 0.5 - world.rand.nextDouble(), -0.5, 0.5 - world.rand.nextDouble());
                }
            }
            int light = WorldRenderer.getCombinedLight(world, pos);
            Minecraft.getInstance().getItemRenderer()
                    .renderItem(beam.renderItem, TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.pop();
        }
    }

    private void renderBeamLine(ItemBeam beam, MatrixStack matrixStack, IRenderTypeBuffer buffer, float progress) {
        int alpha = (int)((1 - Math.abs(progress - 0.5)) * 32 + 16);

        Matrix4f positionMatrix = matrixStack.getLast().getMatrix();
        IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.BEAM_LINE_THICK);
        builder.pos(positionMatrix, beam.startPos.getX(), beam.startPos.getY(), beam.startPos.getZ())
                .color(beam.colors[0], beam.colors[1], beam.colors[2], alpha)
                .endVertex();
        builder.pos(positionMatrix, beam.endPos.getX(), beam.endPos.getY(), beam.endPos.getZ())
                .color(beam.colors[0], beam.colors[1], beam.colors[2], alpha)
                .endVertex();

        IVertexBuilder builder2 = buffer.getBuffer(ModRenderTypes.BEAM_LINE_THIN);
        builder2.pos(positionMatrix, beam.startPos.getX(), beam.startPos.getY(), beam.startPos.getZ())
                .color(beam.colors[0], beam.colors[1], beam.colors[2], alpha * 2)
                .endVertex();
        builder2.pos(positionMatrix, beam.endPos.getX(), beam.endPos.getY(), beam.endPos.getZ())
                .color(beam.colors[0], beam.colors[1], beam.colors[2], alpha * 2)
                .endVertex();
    }
}
