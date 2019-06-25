package me.desht.modularrouters.client.model;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModelBakeEventHandler {
    private ModelBakeEventHandler() {}

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        override(event, ModBlocks.ITEM_ROUTER, CamouflagingModel.RouterModel::new);
        override(event, ModBlocks.TEMPLATE_FRAME, CamouflagingModel.TemplateFrameModel::new);
    }

    private static void override(ModelBakeEvent event, Block block, Function<IBakedModel, CamouflagingModel> f) {
        for (BlockState state : block.getStateContainer().getValidStates()) {
            ModelResourceLocation loc = BlockModelShapes.getModelLocation(state);
            IBakedModel model = event.getModelRegistry().get(loc);
            if (model != null) {
                event.getModelRegistry().put(loc, f.apply(model));
            }
        }
    }
}
