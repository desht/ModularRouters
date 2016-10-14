package me.desht.modularrouters.client;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModelBakeEventHandler {
    private ModelBakeEventHandler() {}

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Object object = event.getModelRegistry().getObject(RouterModel.variantTag);
        if (object instanceof IBakedModel) {
            System.out.println("register router model: " + RouterModel.variantTag);
            IBakedModel existingModel = (IBakedModel)object;
            RouterModel customModel = new RouterModel(existingModel);
            event.getModelRegistry().putObject(RouterModel.variantTag, customModel);
        }
    }
}
