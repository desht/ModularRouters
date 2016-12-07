package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public abstract class ContainerSmartFilter extends Container {
    private final EntityPlayer player;
    private final ItemStack filterStack;
    private final TileEntityItemRouter router;

    public ContainerSmartFilter(EntityPlayer player, ItemStack filterStack, TileEntityItemRouter router) {
        this.player = player;
        this.filterStack = filterStack;
        this.router = router;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    public ItemStack getFilterStack() {
        return filterStack;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public TileEntityItemRouter getRouter() {
        return router;
    }
}
