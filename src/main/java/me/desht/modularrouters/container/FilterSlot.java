package me.desht.modularrouters.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FilterSlot extends SlotItemHandler {
    public FilterSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        return false;
    }
}
