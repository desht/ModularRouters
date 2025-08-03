package me.desht.modularrouters.client.model;

public class ModelBakeEventHandler {
    private ModelBakeEventHandler() {}

//    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
//        override(event, ModBlocks.MODULAR_ROUTER.get(), CamouflagingModel.RouterModel::new);
//        override(event, ModBlocks.TEMPLATE_FRAME.get(), CamouflagingModel.TemplateFrameModel::new);
//    }
//
//    private static void override(ModelEvent.ModifyBakingResult event, Block block, Function<BakedModel, CamouflagingModel> f) {
//        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
//            ModelResourceLocation loc = BlockModelShaper.stateToModelLocation(state);
//            BakedModel model = event.getBakingResult().blockStateModels().get(loc);
//            if (model != null) {
//                event.getBakingResult().blockStateModels().put(loc, f.apply(model));
//            }
//        }
//    }
}
