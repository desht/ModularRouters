package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public abstract class ContainerSmartFilter extends Container {
    private final EntityPlayer player;
    private final ItemStack filterStack;
    private final EnumHand hand;
    private final TileEntityItemRouter router;

    public ContainerSmartFilter(EntityPlayer player, ItemStack filterStack, EnumHand hand, TileEntityItemRouter router) {
        this.player = player;
        this.filterStack = filterStack;
        this.hand = hand;
        this.router = router;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public ItemStack getFilterStack() {
        return filterStack;
    }

    public EnumHand getHand() {
        return hand;
    }

    public TileEntityItemRouter getRouter() {
        return router;
    }
}
