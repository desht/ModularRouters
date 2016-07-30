package me.desht.modularrouters.container;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;

public class FilterHandler implements IItemHandlerModifiable {
    private final ItemStack filterStack;

    private final ItemStack[] filters;
    public FilterHandler(ItemStack filterStack, int size) {
        this.filterStack = filterStack;
        this.filters = new ItemStack[size];

        if (!filterStack.hasTagCompound()) {
            filterStack.setTagCompound(new NBTTagCompound());
        }
        readfromNBT(filterStack.getTagCompound());
    }

    public ItemStack getModuleItemStack() {
        return filterStack;
    }

    public void save() {
        for (int i = 0; i < filters.length; i++) {
            if (filters[i] != null && filters[i].stackSize <= 0) {
                filters[i] = null;
            }
        }
        writeToNBT(filterStack.getTagCompound());
    }

    private void readfromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList("ModuleFilter", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < items.tagCount(); ++i)  {
            NBTTagCompound item = items.getCompoundTagAt(i);
            int slot = item.getInteger("Slot");
            if (slot >= 0 && slot < filters.length) {
                filters[slot] = ItemStack.loadItemStackFromNBT(item);
            }
        }
    }

    private void writeToNBT(NBTTagCompound compound) {
        NBTTagList items = new NBTTagList();

        for (int i = 0; i < getSlots(); i++) {
            if (getStackInSlot(i) != null) {
                NBTTagCompound item = new NBTTagCompound();
                item.setInteger("Slot", i);
                getStackInSlot(i).writeToNBT(item);
                items.appendTag(item);
            }
        }
        compound.setTag("ModuleFilter", items);
    }

    @Override
    public int getSlots() {
        return filters.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return filters[slot];
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!simulate) {
            ItemStack stack1 = ItemStack.copyItemStack(stack);
            stack1.stackSize = 1;
            filters[slot] = stack1;
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!simulate) {
            filters[slot] = null;
        }
        return null;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        filters[slot] = stack;
    }
}
