package me.desht.modularrouters.client;

import me.desht.modularrouters.block.BlockCamo;
import me.desht.modularrouters.util.PropertyObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class CamouflagingModel implements IBakedModel {
    private final IBakedModel baseModel;
    private final PropertyObject<IBlockState> camoProp;

    public CamouflagingModel(IBakedModel baseModel, PropertyObject<IBlockState> camoProp) {
        this.baseModel = baseModel;
        this.camoProp = camoProp;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        try {
            return handleBlockState(state, side, rand);
        } catch (IllegalArgumentException e) {
            return baseModel.getQuads(state, side, rand);
        }
    }

    private List<BakedQuad> handleBlockState(IBlockState state, EnumFacing side, long rand) {
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            IBlockState camoState = ext.getValue(camoProp);
            if (camoState != null) {
                BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
                if (layer != null && camoState.getBlock().canRenderInLayer(camoState, layer)) {
                    BlockModelShapes blockModelShapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
                    return blockModelShapes.getModelForState(camoState).getQuads(camoState, side, rand);
                } else {
                    return Collections.emptyList();
                }
            }
        }

        return baseModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return baseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return baseModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return baseModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return baseModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return baseModel.getOverrides();
    }

    public static class RouterModel extends CamouflagingModel {
        public RouterModel(IBakedModel baseModel) {
            super(baseModel, BlockCamo.CAMOUFLAGE_STATE);
        }
    }

    public static class TemplateFrameModel extends CamouflagingModel {
        public static final ModelResourceLocation VARIANT_TAG
                = new ModelResourceLocation("modularrouters:templateFrame", "normal");

        public TemplateFrameModel(IBakedModel baseModel) {
            super(baseModel, BlockCamo.CAMOUFLAGE_STATE);
        }
    }
}
