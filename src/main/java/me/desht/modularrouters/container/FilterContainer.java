package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public abstract class FilterContainer extends Container {
    public FilterContainer(EntityPlayer player, ItemStack stack, TileEntityItemRouter router) {

    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
