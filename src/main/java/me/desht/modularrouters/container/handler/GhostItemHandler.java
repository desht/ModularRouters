package me.desht.modularrouters.container.handler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class GhostItemHandler extends ItemStackHandler {
    public GhostItemHandler(int size) {
        super(size);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!simulate) {
            ItemStack stack1 = stack.copy();
            stack1.setCount(1);
            setStackInSlot(slot, stack1);
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!simulate) {
            setStackInSlot(slot, ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }
}
