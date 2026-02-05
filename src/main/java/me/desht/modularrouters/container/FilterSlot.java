package me.desht.modularrouters.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/**
 * A slot for filter items that can be set but not picked up normally.
 */
public class FilterSlot extends ResourceHandlerSlot {
    public <S extends StacksResourceHandler<ItemStack, ItemResource>> FilterSlot(S handler, int index, int xPosition, int yPosition) {
        super(handler, handler::set, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return false;
    }
}
