package me.desht.modularrouters.client.model;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.block.BlockCamo;
import me.desht.modularrouters.block.tile.ICamouflageable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * With credit to Botania for showing me how this can be made to work with connected textures (trick being
 * to pass IBlockAccess/BlockPos via extended state).
 *
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/client/model/PlatformModel.java
 */
public abstract class CamouflagingModel implements IDynamicBakedModel {
    private final IBakedModel baseModel;

    CamouflagingModel(IBakedModel baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData modelData) {
        if (state == null || !(state.getBlock() instanceof BlockCamo)) {
            return baseModel.getQuads(state, side, rand, modelData);
        }
        BlockState camoState = modelData.getData(BlockCamo.CAMOUFLAGE_STATE);
        IEnviromentBlockReader blockAccess = modelData.getData(BlockCamo.BLOCK_ACCESS);
        BlockPos pos = modelData.getData(BlockCamo.BLOCK_POS);
        if (blockAccess == null || pos == null) {
            return baseModel.getQuads(state, side, rand, modelData);
        }

        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer == null) {
            layer = BlockRenderLayer.SOLID; // workaround for when this isn't set (digging, etc.)
        }
        if (camoState == null && layer == BlockRenderLayer.SOLID) {
            // No camo
            return baseModel.getQuads(state, side, rand, modelData);
        } else if (camoState != null && camoState.getBlock().canRenderInLayer(camoState, layer)) {
            // Steal camo's model
            IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(camoState);

            // Their model can be smart too
            BlockState extended = camoState.getBlock().getExtendedState(camoState, new FakeBlockAccess(blockAccess), pos);
            return model.getQuads(extended, side, rand, model.getModelData(blockAccess, pos, extended, modelData));
        }

        return ImmutableList.of(); // Nothing renders
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IEnviromentBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        return tileData;
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

    private static class FakeBlockAccess implements IBlockReader {
        private final IBlockReader compose;

        private FakeBlockAccess(IBlockReader compose) {
            this.compose = compose;
        }

        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return compose.getTileEntity(pos);
        }

        @Nonnull
        @Override
        public BlockState getBlockState(@Nonnull BlockPos pos) {
            BlockState state = compose.getBlockState(pos);
            if (state.getBlock() instanceof BlockCamo) {
                TileEntity te = compose.getTileEntity(pos);
                if (te instanceof ICamouflageable) {
                    state = ((ICamouflageable) te).getCamouflage();
                }
            }
            return state == null ? Blocks.AIR.getDefaultState() : state;
        }

        @Nonnull
        @Override
        public IFluidState getFluidState(@Nonnull BlockPos blockPos) {
            // todo test for 1.13
            return compose.getFluidState(blockPos);
        }

    }

    static class RouterModel extends CamouflagingModel {
        RouterModel(IBakedModel baseModel) {
            super(baseModel);
        }
    }

    static class TemplateFrameModel extends CamouflagingModel {
        TemplateFrameModel(IBakedModel baseModel) {
            super(baseModel);
        }
    }
}
