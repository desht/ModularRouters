package me.desht.modularrouters.client;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ModelBakeEventHandler {
    private ModelBakeEventHandler() {}

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        // todo 1.13
//        // item router
//        Map<IBlockState, ModelResourceLocation> map
//                = event.getModelManager().getBlockModelShapes().getBlockStateMapper().getVariants(ObjectRegistry.ITEM_ROUTER);
//        for (Map.Entry<IBlockState,ModelResourceLocation> entry : map.entrySet()) {
//            Object object = event.getModelRegistry().getObject(entry.getValue());
//            if (object != null) {
//                IBakedModel existing = (IBakedModel) object;
//                RouterModel customModel = new RouterModel(existing);
//                event.getModelRegistry().putObject(entry.getValue(), customModel);
//            }
//        }
//
//        // template frame
//        Object object = event.getModelRegistry().getObject(TemplateFrameModel.VARIANT_TAG);
//        if (object != null) {
//            IBakedModel existingModel = (IBakedModel) object;
//            TemplateFrameModel customModel = new TemplateFrameModel(existingModel);
//            event.getModelRegistry().putObject(TemplateFrameModel.VARIANT_TAG, customModel);
//        }
    }
}
