package me.desht.modularrouters.client.model;

import me.desht.modularrouters.block.CamouflageableBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.List;

// TODO only partially works
public abstract class CamouflagingModel extends DelegateBlockStateModel {
    protected CamouflagingModel(BlockStateModel delegate) {
        super(delegate);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
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
        } else {
            // Steal camo's model
            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(camoState);
            model.collectParts(level, pos, state, random, parts);
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
