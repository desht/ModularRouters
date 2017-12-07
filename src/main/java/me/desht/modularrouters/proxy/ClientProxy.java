package me.desht.modularrouters.proxy;

import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.AreaShowManager;
import me.desht.modularrouters.client.ItemColours;
import me.desht.modularrouters.client.ModelBakeEventHandler;
import me.desht.modularrouters.client.fx.FXSparkle;
import me.desht.modularrouters.client.fx.RenderListener;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        super.preInit();

        MinecraftForge.EVENT_BUS.register(ModelBakeEventHandler.class);
        MinecraftForge.EVENT_BUS.register(AreaShowManager.getInstance());
    }

    @Override
    public void init() {
        super.init();

        MinecraftForge.EVENT_BUS.register(RenderListener.class);

        registerBlockColors();
    }

    @Override
    public void postInit() {
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemColours.ModuleColour(), RegistrarMR.MODULE);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemColours.UpgradeColour(), RegistrarMR.UPGRADE);
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
        return Minecraft.getMinecraft().world;
    }

    @Override
    public IThreadListener threadListener() {
        return Minecraft.getMinecraft();
    }

    @Override
    public TileEntityItemRouter getOpenItemRouter() {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
            return ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).router;
        } else {
            return null;
        }
    }

    private void registerBlockColors() {
        // this ensures camouflage properly mimics colourable blocks like grass blocks
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
            if (pos == null) return -1;
            TileEntity te = MiscUtil.getTileEntitySafely(worldIn, pos);
            if (te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null) {
                return Minecraft.getMinecraft().getBlockColors().colorMultiplier(((ICamouflageable) te).getCamouflage(), te.getWorld(), pos, tintIndex);
            } else {
                return 0xffffff;
            }
        }, RegistrarMR.ITEM_ROUTER, RegistrarMR.TEMPLATE_FRAME);
    }

    @Override
    public EntityPlayer getPlayer() {
        return Minecraft.getMinecraft().player;
    }
}
