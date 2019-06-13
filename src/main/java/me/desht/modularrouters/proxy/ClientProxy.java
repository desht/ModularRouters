package me.desht.modularrouters.proxy;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.fx.FXSparkle;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.client.gui.upgrade.GuiSyncUpgrade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ClientProxy implements IProxy {
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
        Minecraft.getInstance().particles.addEffect(sparkle);
    }

    private boolean doParticle(World world) {
        if(!world.isRemote)
            return false;

//        if(!ConfigHandler.useVanillaParticleLimiter)
//            return true;

        float chance = 1F;
        if(Minecraft.getInstance().gameSettings.particles == ParticleStatus.DECREASED)
            chance = 0.6F;
        else if(Minecraft.getInstance().gameSettings.particles == ParticleStatus.MINIMAL)
            chance = 0.2F;

        return chance == 1F || Math.random() < chance;
    }

    @Override
    public World theClientWorld() {
        return Minecraft.getInstance().world;
    }

    @Override
    public TileEntityItemRouter getOpenItemRouter() {
        if (Minecraft.getInstance().field_71462_r instanceof GuiItemRouter) {
            return ((GuiItemRouter) Minecraft.getInstance().field_71462_r).router;
        } else {
            return null;
        }
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void openSyncGui(ItemStack stack, Hand hand) {
        Minecraft.getInstance().displayGuiScreen(new GuiSyncUpgrade(stack, hand));
    }
}
