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
        Map<IBlockState,ModelResourceLocation> map
                = event.getModelManager().getBlockModelShapes().getBlockStateMapper().getVariants(ModBlocks.itemRouter);
        for (Map.Entry<IBlockState,ModelResourceLocation> entry : map.entrySet()) {
            Object object = event.getModelRegistry().getObject(entry.getValue());
            if (object != null) {
                IBakedModel existing = (IBakedModel) object;
                RouterModel customModel = new RouterModel(existing);
                event.getModelRegistry().putObject(entry.getValue(), customModel);
            }
        }
    }
}
