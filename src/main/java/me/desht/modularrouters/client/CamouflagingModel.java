package me.desht.modularrouters.client;

import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.BlockTemplateFrame;
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
import java.util.List;

import static me.desht.modularrouters.client.EmptyQuadsModel.NO_QUADS;

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
            return handleBlockState(state).getQuads(state, side, rand);
        } catch (IllegalArgumentException e) {
            return baseModel.getQuads(state, side, rand);
        }
    }

    private IBakedModel handleBlockState(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            IBlockState camoState = ext.getValue(camoProp);
            if (camoState != null) {
                BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
                if (camoState.getBlock().canRenderInLayer(camoState, layer)) {
                    BlockModelShapes blockModelShapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
                    return blockModelShapes.getModelForState(camoState);
                } else {
                    return NO_QUADS;
                }
            }
        }

        return baseModel;
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
            super(baseModel, BlockItemRouter.CAMOUFLAGE_STATE);
        }
    }

    public static class TemplateFrameModel extends CamouflagingModel {
        public static final ModelResourceLocation VARIANT_TAG
                = new ModelResourceLocation("modularrouters:templateFrame", "normal");

        public TemplateFrameModel(IBakedModel baseModel) {
            super(baseModel, BlockTemplateFrame.CAMOUFLAGE_STATE);
        }
    }
}
