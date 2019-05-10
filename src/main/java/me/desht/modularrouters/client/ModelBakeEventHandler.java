package me.desht.modularrouters.client;

import me.desht.modularrouters.core.ObjectRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Function;

public class ModelBakeEventHandler {
    private ModelBakeEventHandler() {}

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        override(event, ObjectRegistry.ITEM_ROUTER, CamouflagingModel.RouterModel::new);
        override(event, ObjectRegistry.TEMPLATE_FRAME, CamouflagingModel.TemplateFrameModel::new);
    }

    private static void override(ModelBakeEvent event, Block block, Function<IBakedModel, CamouflagingModel> f) {
        for (IBlockState state : block.getStateContainer().getValidStates()) {
            ModelResourceLocation loc = BlockModelShapes.getModelLocation(state);
            IBakedModel model = event.getModelRegistry().get(loc);
            if (model != null) {
                event.getModelRegistry().put(loc, f.apply(model));
            }
        }
    }
}
