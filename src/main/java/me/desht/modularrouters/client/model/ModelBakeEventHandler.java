package me.desht.modularrouters.client.model;

import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.function.Function;

public class ModelBakeEventHandler {
    private ModelBakeEventHandler() {}

    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        override(event, ModBlocks.MODULAR_ROUTER.get(), CamouflagingModel.RouterModel::new);
        override(event, ModBlocks.TEMPLATE_FRAME.get(), CamouflagingModel.TemplateFrameModel::new);
    }

    private static void override(ModelEvent.ModifyBakingResult event, Block block, Function<BlockStateModel, CamouflagingModel> f) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            BlockStateModel model = event.getBakingResult().blockStateModels().get(state);
            if (model != null) {
                event.getBakingResult().blockStateModels().put(state, f.apply(model));
            }
        }
    }
}
