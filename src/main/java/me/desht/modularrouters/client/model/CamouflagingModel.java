package me.desht.modularrouters.client.model;

import me.desht.modularrouters.block.BlockCamo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class CamouflagingModel implements IDynamicBakedModel {
    private final BakedModel baseModel;

    CamouflagingModel(BakedModel baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData modelData) {
        if (state == null || !(state.getBlock() instanceof BlockCamo)) {
            return baseModel.getQuads(state, side, rand, modelData);
        }
        BlockState camoState = modelData.getData(BlockCamo.CAMOUFLAGE_STATE);

        RenderType layer = MinecraftForgeClient.getRenderLayer();
        if (layer == null) {
            layer = RenderType.solid(); // workaround for when this isn't set (digging, etc.)
        }
        if (camoState == null && layer == RenderType.solid()) {
            // No camo
            return baseModel.getQuads(state, side, rand, modelData);
        } else if (camoState != null && ItemBlockRenderTypes.canRenderInLayer(camoState, layer)) {
            // Steal camo's model
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(camoState);
            return model.getQuads(camoState, side, rand, modelData);
        } else {
            // Not rendering in this layer
            return Collections.emptyList();
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseModel.isGui3d();
    }

    @Override
    public boolean isCustomRenderer() {
        return baseModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return baseModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return baseModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return baseModel.getOverrides();
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    static class RouterModel extends CamouflagingModel {
        RouterModel(BakedModel baseModel) {
            super(baseModel);
        }
    }

    static class TemplateFrameModel extends CamouflagingModel {
        TemplateFrameModel(BakedModel baseModel) {
            super(baseModel);
        }
    }
}
