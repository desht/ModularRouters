package me.desht.modularrouters.client;

import me.desht.modularrouters.block.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class ModelBakeEventHandler {
    private ModelBakeEventHandler() {}

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        // item router
        Map<IBlockState,ModelResourceLocation> map
                = event.getModelManager().getBlockModelShapes().getBlockStateMapper().getVariants(ModBlocks.itemRouter);
        for (Map.Entry<IBlockState,ModelResourceLocation> entry : map.entrySet()) {
            Object object = event.getModelRegistry().getObject(entry.getValue());
            if (object != null) {
                IBakedModel existing = (IBakedModel) object;
                CamouflagingModel.RouterModel customModel = new CamouflagingModel.RouterModel(existing);
                event.getModelRegistry().putObject(entry.getValue(), customModel);
            }
        }

        // template frame
        Object object = event.getModelRegistry().getObject(CamouflagingModel.TemplateFrameModel.variantTag);
        if (object != null) {
            IBakedModel existingModel = (IBakedModel) object;
            CamouflagingModel.TemplateFrameModel customModel = new CamouflagingModel.TemplateFrameModel(existingModel);
            event.getModelRegistry().putObject(CamouflagingModel.TemplateFrameModel.variantTag, customModel);
        }
    }
}
