package me.desht.modularrouters.client;

import me.desht.modularrouters.block.BlockItemRouter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.List;

public class RouterModel implements IBakedModel {
    private final IBakedModel uncamouflagedModel;

    public RouterModel(IBakedModel existingModel) {
        this.uncamouflagedModel = existingModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        try {
            return handleBlockState(state).getQuads(state, side, rand);
        } catch (IllegalArgumentException e) {
            return uncamouflagedModel.getQuads(state, side, rand);
        }
    }

    private IBakedModel handleBlockState(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            IBlockState camoState = ext.getValue(BlockItemRouter.CAMOUFLAGE_STATE);
            BlockModelShapes blockModelShapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
            if (camoState != null) {
                return blockModelShapes.getModelForState(camoState);
            }
        }

        return uncamouflagedModel;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return uncamouflagedModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return uncamouflagedModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return uncamouflagedModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return uncamouflagedModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return uncamouflagedModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return uncamouflagedModel.getOverrides();
    }
}
