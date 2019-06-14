package me.desht.modularrouters.proxy;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public interface IProxy {

    World theClientWorld();

    TileEntityItemRouter getOpenItemRouter();

    PlayerEntity getClientPlayer();

    void openSyncGui(ItemStack stack, Hand hand);
}
