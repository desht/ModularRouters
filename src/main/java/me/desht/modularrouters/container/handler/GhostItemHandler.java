package me.desht.modularrouters.container.handler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Handler for a ghost inventory; only holds copies of items, and can't be extracted from.
 */
public class GhostItemHandler implements IItemHandlerModifiable, INBTSerializable<NBTTagList> {
    protected final ItemStack[] items;

    public GhostItemHandler(int size) {
        this.items = new ItemStack[size];
        Arrays.fill(this.items, ItemStack.EMPTY);
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList items = new NBTTagList();
        for (int i = 0; i < getSlots(); i++) {
            if (!getStackInSlot(i).isEmpty()) {
                NBTTagCompound item = new NBTTagCompound();
                item.setInteger("Slot", i);
                getStackInSlot(i).writeToNBT(item);
                items.appendTag(item);
            }
        }
        return items;
    }

    @Override
    public void deserializeNBT(NBTTagList list) {
        for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            int slot = compound.getInteger("Slot");
            if (slot >= 0 && slot < items.length) {
                // not possible under normal circumstances, but if a modded item were in the filter, and that mod
                // gets removed, an empty item would be possible.  https://github.com/desht/ModularRouters/issues/54
                ItemStack stack = new ItemStack(compound);
                if (!stack.isEmpty()) items[slot] = stack;
            }
        }
    }
    @Override
    public int getSlots() {
        return items.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return items[slot];
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!simulate) {
            ItemStack stack1 = stack.copy();
            stack1.setCount(1);
            items[slot] = stack1;
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!simulate) {
            items[slot] = ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        items[slot] = stack;
    }
}
