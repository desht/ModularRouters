package me.desht.modularrouters.client;

import me.desht.modularrouters.client.CamouflagingModel.RouterModel;
import me.desht.modularrouters.client.CamouflagingModel.TemplateFrameModel;
import me.desht.modularrouters.core.RegistrarMR;
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
                = event.getModelManager().getBlockModelShapes().getBlockStateMapper().getVariants(RegistrarMR.ITEM_ROUTER);
        for (Map.Entry<IBlockState,ModelResourceLocation> entry : map.entrySet()) {
            Object object = event.getModelRegistry().getObject(entry.getValue());
            if (object != null) {
                IBakedModel existing = (IBakedModel) object;
                RouterModel customModel = new RouterModel(existing);
                event.getModelRegistry().putObject(entry.getValue(), customModel);
            }
        }

        // template frame
        Object object = event.getModelRegistry().getObject(TemplateFrameModel.VARIANT_TAG);
        if (object != null) {
            IBakedModel existingModel = (IBakedModel) object;
            TemplateFrameModel customModel = new TemplateFrameModel(existingModel);
            event.getModelRegistry().putObject(TemplateFrameModel.VARIANT_TAG, customModel);
        }
    }
}
