package me.desht.modularrouters.client.model;

import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Function;

public class ModelBakeEventHandler {
    private ModelBakeEventHandler() {}

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        override(event, ModBlocks.ITEM_ROUTER.get(), CamouflagingModel.RouterModel::new);
        override(event, ModBlocks.TEMPLATE_FRAME.get(), CamouflagingModel.TemplateFrameModel::new);
    }

    private static void override(ModelBakeEvent event, Block block, Function<BakedModel, CamouflagingModel> f) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            ModelResourceLocation loc = BlockModelShaper.stateToModelLocation(state);
            BakedModel model = event.getModelRegistry().get(loc);
            if (model != null) {
                event.getModelRegistry().put(loc, f.apply(model));
            }
        }
    }
}
