package me.desht.modularrouters.client;

import me.desht.modularrouters.block.BlockTemplateFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

public class TemplateFrameModel implements IPerspectiveAwareModel {
    public static final ModelResourceLocation variantTag
            = new ModelResourceLocation("modularrouters:templateFrame", "normal");

    private IBakedModel existingModel;

    public TemplateFrameModel(IBakedModel existingModel) {
        this.existingModel = existingModel;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        if (existingModel instanceof IPerspectiveAwareModel) {
            Matrix4f matrix4f = ((IPerspectiveAwareModel) existingModel).handlePerspective(cameraTransformType).getRight();
            return Pair.of(this, matrix4f);
        } else {
            // If the parent model isn't an IPerspectiveAware, we'll need to generate the correct matrix ourselves using the
            //  ItemCameraTransforms.

            ItemCameraTransforms itemCameraTransforms = existingModel.getItemCameraTransforms();
            ItemTransformVec3f itemTransformVec3f = itemCameraTransforms.getTransform(cameraTransformType);
            TRSRTransformation tr = new TRSRTransformation(itemTransformVec3f);
            Matrix4f mat = null;
            if (tr != null) { // && tr != TRSRTransformation.identity()) {
                mat = tr.getMatrix();
            }
            // The TRSRTransformation for vanilla items have blockCenterToCorner() applied, however handlePerspective
            //  reverses it back again with blockCornerToCenter().  So we don't need to apply it here.

            return Pair.of(this, mat);
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        try {
            return handleBlockState(state).getQuads(state, side, rand);
        } catch (IllegalArgumentException e) {
            return existingModel.getQuads(state, side, rand);
        }
    }

    private IBakedModel handleBlockState(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            IBlockState camoState = ext.getValue(BlockTemplateFrame.CAMOUFLAGE_STATE);
            if (camoState != null) {
                BlockModelShapes blockModelShapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
                return blockModelShapes.getModelForState(camoState);
            }
        }
        return existingModel;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return existingModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return existingModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return existingModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return existingModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return existingModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return existingModel.getOverrides();
    }
}
