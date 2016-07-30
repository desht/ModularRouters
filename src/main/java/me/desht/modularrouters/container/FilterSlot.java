package me.desht.modularrouters.container;

import net.minecraftforge.items.SlotItemHandler;

public class FilterSlot extends SlotItemHandler {
    private final FilterHandler filterHandler;

    public FilterSlot(FilterHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        filterHandler = itemHandler;
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        filterHandler.save();
    }
}
