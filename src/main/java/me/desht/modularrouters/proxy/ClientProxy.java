package me.desht.modularrouters.proxy;

import amerifrance.guideapi.api.GuideAPI;
import amerifrance.guideapi.api.impl.Book;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.ModelBakeEventHandler;
import me.desht.modularrouters.client.TemplateFrameModel;
import me.desht.modularrouters.client.fx.FXSparkle;
import me.desht.modularrouters.client.fx.RenderListener;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.gui.GuiItemRouter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.item.Item;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import scala.collection.parallel.ParIterableLike;

import javax.annotation.Nullable;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        super.preInit();

        MinecraftForge.EVENT_BUS.register(Config.ConfigEventHandler.class);

        // the can_emit property has no effect on block rendering, so let's not create unnecessary variants
        ModelLoader.setCustomStateMapper(ModBlocks.itemRouter, new StateMap.Builder().ignore(BlockItemRouter.CAN_EMIT).build());

        StateMapperBase ignoreState = new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
                return TemplateFrameModel.variantTag;
            }
        };
        ModelLoader.setCustomStateMapper(ModBlocks.templateFrame, ignoreState);

        MinecraftForge.EVENT_BUS.register(ModelBakeEventHandler.class);

    }

    @Override
    public void init() {
        super.init();

        MinecraftForge.EVENT_BUS.register(RenderListener.class);
        registerBlockColors();
    }

    @Override
    public void registerItemRenderer(Item item, int meta, String id) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(ModularRouters.modId + ":" + id, "inventory"));
    }

    private static boolean noclipEnabled = false;
    private static boolean corruptSparkle = false;

    @Override
    public void setSparkleFXNoClip(boolean noclip) {
        noclipEnabled = noclip;
    }

    @Override
    public void setSparkleFXCorrupt(boolean corrupt) {
        corruptSparkle = corrupt;
    }

    @Override
    public void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float size, int m, boolean fake) {
        if (!doParticle(world) && !fake)
            return;

        FXSparkle sparkle = new FXSparkle(world, x, y, z, size, r, g, b, m);
        sparkle.fake = sparkle.noClip = fake;
        if (noclipEnabled)
            sparkle.noClip = true;
        if (corruptSparkle)
            sparkle.corrupt = true;
        Minecraft.getMinecraft().effectRenderer.addEffect(sparkle);
    }

    private boolean doParticle(World world) {
        if(!world.isRemote)
            return false;

//        if(!ConfigHandler.useVanillaParticleLimiter)
//            return true;

        float chance = 1F;
        if(Minecraft.getMinecraft().gameSettings.particleSetting == 1)
            chance = 0.6F;
        else if(Minecraft.getMinecraft().gameSettings.particleSetting == 2)
            chance = 0.2F;

        return chance == 1F || Math.random() < chance;
    }

    @Override
    public World theClientWorld() {
        return Minecraft.getMinecraft().theWorld;
    }

    @Override
    public IThreadListener threadListener() {
        return Minecraft.getMinecraft();
    }

    @Override
    public void addGuidebookModel(Book guideBook) {
        GuideAPI.setModel(guideBook);
    }

    @Override
    public TileEntityItemRouter getOpenItemRouter() {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
            return ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).router;
        } else {
            return null;
        }
    }

    @Override
    public void registerBlockColors() {
        // this ensures the camo upgrade properly mimics colourable blocks like grass blocks
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
            TileEntityItemRouter te = TileEntityItemRouter.getRouterAt(worldIn, pos);
            if (te != null && te.getCamouflage() != null) {
                return Minecraft.getMinecraft().getBlockColors().colorMultiplier(te.getCamouflage(), te.getWorld(), pos, tintIndex);
            } else {
                return -1;
            }
        }, ModBlocks.itemRouter);
    }
}
