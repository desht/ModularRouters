package me.desht.modularrouters.proxy;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.fx.FXSparkle;
import me.desht.modularrouters.client.fx.RenderListener;
import me.desht.modularrouters.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        super.preInit();
        MinecraftForge.EVENT_BUS.register(new Config.ConfigEventHandler());
    }

    @Override
    public void init() {
        super.init();
        MinecraftForge.EVENT_BUS.register(RenderListener.class);
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
}
