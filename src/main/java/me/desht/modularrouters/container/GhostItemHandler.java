package me.desht.modularrouters.container;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Handler for a ghost inventory; only holds copies of items, and can't be extracted from.
 */
public class GhostItemHandler implements IItemHandlerModifiable, INBTSerializable<NBTTagList> {
    protected final ItemStack[] items;

    public GhostItemHandler(int size) {
        this.items = new ItemStack[size];
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList items = new NBTTagList();
        for (int i = 0; i < getSlots(); i++) {
            if (getStackInSlot(i) != null) {
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
            NBTTagCompound item = list.getCompoundTagAt(i);
            int slot = item.getInteger("Slot");
            if (slot >= 0 && slot < items.length) {
                items[slot] = ItemStack.loadItemStackFromNBT(item);
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

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!simulate) {
            ItemStack stack1 = ItemStack.copyItemStack(stack);
            stack1.stackSize = 1;
            items[slot] = stack1;
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!simulate) {
            items[slot] = null;
        }
        return null;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        items[slot] = stack;
    }
}
