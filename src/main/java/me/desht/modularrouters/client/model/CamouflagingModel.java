package me.desht.modularrouters.client.model;

public abstract class CamouflagingModel /*implements IDynamicBakedModel*/ {
//    private final BakedModel baseModel;
//
//    CamouflagingModel(BakedModel baseModel) {
//        this.baseModel = baseModel;
//    }
//
//    @Override
//    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData modelData, @Nullable RenderType renderType) {
//        if (state == null || !(state.getBlock() instanceof CamouflageableBlock)) {
//            return baseModel.getQuads(state, side, rand, modelData, renderType);
//        }
//        BlockState camoState = modelData.get(CamouflageableBlock.CAMOUFLAGE_STATE);
//
//        if (renderType == null) {
//            renderType = RenderType.solid(); // workaround for when this isn't set (digging, etc.)
//        }
//        if ((camoState == null || camoState.getBlock() instanceof CamouflageableBlock) && renderType == RenderType.solid()) {
//            // No camo (or bad camo!)
//            return baseModel.getQuads(state, side, rand, modelData, renderType);
//        } else if (camoState != null && getRenderTypes(camoState, rand, modelData).contains(renderType)) {
//            // Steal camo's model
//            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(camoState);
//            return model.getQuads(camoState, side, rand, modelData, renderType);
//        } else {
//            // Not rendering in this layer
//            return List.of();
//        }
//    }
//
//    @Override
//    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
//        BlockState camoState = data.get(CamouflageableBlock.CAMOUFLAGE_STATE);
//        return IDynamicBakedModel.super.getRenderTypes(camoState == null ? state : camoState, rand, data);
//    }
//
//    @Override
//    public boolean useAmbientOcclusion() {
//        return baseModel.useAmbientOcclusion();
//    }
//
//    @Override
//    public boolean isGui3d() {
//        return baseModel.isGui3d();
//    }
//
////    @Override
////    public boolean isCustomRenderer() {
////        return baseModel.isCustomRenderer();
////    }
//
//    @Override
//    public TextureAtlasSprite getParticleIcon() {
//        return baseModel.getParticleIcon();
//    }
//
//    @Override
//    public ItemTransforms getTransforms() {
//        return baseModel.getTransforms();
//    }
//
////    @Override
////    public BakedOverrides overrides() {
////        return baseModel.overrides();
////    }
//
//    @Override
//    public boolean usesBlockLight() {
//        return false;
//    }
//
//    static class RouterModel extends CamouflagingModel {
//        RouterModel(BakedModel baseModel) {
//            super(baseModel);
//        }
//    }
//
//    static class TemplateFrameModel extends CamouflagingModel {
//        TemplateFrameModel(BakedModel baseModel) {
//            super(baseModel);
//        }
//    }
}
