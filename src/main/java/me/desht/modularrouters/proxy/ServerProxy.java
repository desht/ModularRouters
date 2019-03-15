package me.desht.modularrouters.proxy;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ServerProxy implements IProxy {
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
    public TileEntityItemRouter getOpenItemRouter() {
        return null;
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return null;
    }

    @Override
    public void openSyncGui(ItemStack stack, EnumHand hand) {
    }

}
