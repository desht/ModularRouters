package me.desht.modularrouters.proxy;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IProxy {

    void setSparkleFXNoClip(boolean noclip);

    void setSparkleFXCorrupt(boolean noclip);

    void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float size, int m, boolean fake);

    default void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float size, int m)  {
        sparkleFX(world, x, y, z, r, g, b, size, m, false);
    }

    World theClientWorld();

    TileEntityItemRouter getOpenItemRouter();

    EntityPlayer getClientPlayer();
}
