package me.desht.modularrouters.client.model;

import me.desht.modularrouters.block.CamouflageableBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.List;

// TODO only partially works
public abstract class CamouflagingModel extends DelegateBlockStateModel/*implements IDynamicBakedModel*/ {
    protected CamouflagingModel(BlockStateModel delegate) {
        super(delegate);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        if (state == null || !(state.getBlock() instanceof CamouflageableBlock)) {
            super.collectParts(level, pos, state, random, parts);
        }
        ModelData modelData = level.getModelData(pos);
        BlockState camoState = modelData.get(CamouflageableBlock.CAMOUFLAGE_STATE);

//        if (renderType == null) {
//            renderType = RenderType.solid(); // workaround for when this isn't set (digging, etc.)
//        }

        if ((camoState == null || camoState.getBlock() instanceof CamouflageableBlock) /*&& renderType == RenderType.solid()*/) {
            // No camo (or bad camo!)
            super.collectParts(level, pos, state, random, parts);
        } else if (camoState != null /*&& getRenderTypes(camoState, rand, modelData).contains(renderType)*/) {
            // Steal camo's model
            BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(camoState);
            model.collectParts(level, pos, state, random, parts);
        } else {
            // Not rendering in this layer
        }
    }

    static class RouterModel extends CamouflagingModel {
        RouterModel(BlockStateModel baseModel) {
            super(baseModel);
        }
    }

    static class TemplateFrameModel extends CamouflagingModel {
        TemplateFrameModel(BlockStateModel baseModel) {
            super(baseModel);
        }
    }
}
