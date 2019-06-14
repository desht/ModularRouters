package me.desht.modularrouters.proxy;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ServerProxy implements IProxy {
    @Override
    public World theClientWorld() {
        return null;
    }

    @Override
    public TileEntityItemRouter getOpenItemRouter() {
        return null;
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return null;
    }

    @Override
    public void openSyncGui(ItemStack stack, Hand hand) {
    }

}
