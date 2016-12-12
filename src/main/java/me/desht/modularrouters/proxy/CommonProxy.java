package me.desht.modularrouters.proxy;

import amerifrance.guideapi.api.impl.Book;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.Item;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;

public class CommonProxy {

    public void registerItemRenderer(Item item, int meta, String id) {
    }

    public void preInit() {
    }

    public void init() {
    }

    public void postInit() {
    }

    public void setSparkleFXNoClip(boolean noclip) {}

    public void setSparkleFXCorrupt(boolean noclip) {}

    public void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float size, int m) {
        sparkleFX(world, x, y, z, r, g, b, size, m, false);
    }

    public void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float size, int m, boolean fake) {}

    public World theClientWorld() {
        return null;
    }

    public IThreadListener threadListener() {
        return null;
    }

    public TileEntityItemRouter getOpenItemRouter() {
        return null;
    }
}
