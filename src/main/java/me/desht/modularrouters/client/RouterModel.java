package me.desht.modularrouters.client;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.ModBlocks;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class RouterModel implements IBakedModel {
    public static final ModelResourceLocation variantTag = new ModelResourceLocation(ModularRouters.modId + ":" + BlockItemRouter.BLOCK_NAME, "normal");
    private final IBakedModel existingModel;

    public RouterModel(IBakedModel existingModel) {
        this.existingModel = existingModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return handleBlockState(state).getQuads(state, side, rand);
//        if (state.getBlock() != ModBlocks.itemRouter) {
//            return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel().getQuads(state, side, rand);
//        }
//
//        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
//        IBlockState heldState = ((IExtendedBlockState) state).getValue(BlockItemRouter.HELD_STATE);
//		IBlockAccess heldWorld = ((IExtendedBlockState) state).getValue(BlockItemRouter.HELD_WORLD);
//		BlockPos heldPos = ((IExtendedBlockState) state).getValue(BlockItemRouter.HELD_POS);
//
//        if (heldWorld == null || heldPos == null) {
//            return ImmutableList.of();
//        }
//        System.out.println("getQuads!");
//
//        Minecraft mc = Minecraft.getMinecraft();
//        if (heldState == null && layer == BlockRenderLayer.SOLID) {
//            System.out.println("- no camo!");
//            // no camo
//            Map<IProperty<?>, Comparable<?>> map = state.getProperties();
//            List<String> l = Lists.newArrayList();
//            for (IProperty prop : map.keySet()) {
//                l.add(prop.getName() + "=" + state.getValue(prop));
//            }
//            String variant = Joiner.on(",").join(l);
//            System.out.println("not camo'd: " + variant);
//            ModelResourceLocation path = new ModelResourceLocation(ModularRouters.modId + ":" + BlockItemRouter.BLOCK_NAME, variant);
//            return mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(path).getQuads(state, side, rand);
//        } else if (heldState != null) {
//        }
//
//        return ImmutableList.of();
    }

    private IBakedModel handleBlockState(IBlockState state) {
        IBakedModel ret = existingModel;

        if (state instanceof IExtendedBlockState) {
            System.out.println("handle block state!");
            IExtendedBlockState ext = (IExtendedBlockState) state;
            IBlockState camoState = ext.getValue(BlockItemRouter.HELD_STATE);

            Minecraft mc = Minecraft.getMinecraft();
            BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
            BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
            if (camoState != null) {
                System.out.println("- camo'd!");
                ret = blockModelShapes.getModelForState(camoState);
            } else {
//                Map<IProperty<?>, Comparable<?>> map = state.getProperties();
//                List<String> l = Lists.newArrayList();
//                for (IProperty prop : map.keySet()) {
//                    if (prop != BlockItemRouter.CAN_EMIT) {
//                        l.add(prop.getName() + "=" + state.getValue(prop));
//                    }
//                }
//                String variant = Joiner.on(",").join(l);
//                ModelResourceLocation path = new ModelResourceLocation(ModularRouters.modId + ":" + BlockItemRouter.BLOCK_NAME, variant);
//                System.out.println("not camo'd: " + path);
                ret = blockModelShapes.getModelForState(state);
            }
        }

        return ret;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return existingModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return existingModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return existingModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return existingModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return existingModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return existingModel.getOverrides();
    }
}
