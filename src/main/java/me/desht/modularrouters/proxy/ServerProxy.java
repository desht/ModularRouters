package me.desht.modularrouters.proxy;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;

public class ServerProxy implements IProxy {

    @Override
    public void preInit() {
    }

    @Override
    public void init() {
    }

    @Override
    public void postInit() {
    }

    @Override
    public void setSparkleFXNoClip(boolean noclip) {}

    @Override
    public void setSparkleFXCorrupt(boolean noclip) {}

    @Override
    public void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float size, int m, boolean fake) {}

    @Override
    public World theClientWorld() {
        return null;
    }

    @Override
    public IThreadListener threadListener() {
        return null;
    }

    @Override
    public TileEntityItemRouter getOpenItemRouter() {
        return null;
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return null;
    }

}
