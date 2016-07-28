package me.desht.modularrouters.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class ModuleInventory implements IInventory {
    public static final int N_FILTER_SLOTS = 9;

    private final String name;
    public final ItemStack moduleItem;
    private ItemStack[] filters = new ItemStack[N_FILTER_SLOTS];

    public ModuleInventory(ItemStack stack) {
        this.moduleItem = stack;
        this.name = stack.getDisplayName();

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        readFromNBT(stack.getTagCompound());
    }

    private void readFromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList("ModuleFilter", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < items.tagCount(); ++i)  {
			NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(i);
			int slot = item.getInteger("Slot");
			if (slot >= 0 && slot < getSizeInventory()) {
				filters[slot] = ItemStack.loadItemStackFromNBT(item);
			}
		}
	}

    private void writeToNBT(NBTTagCompound compound) {
		NBTTagList items = new NBTTagList();

		for (int i = 0; i < getSizeInventory(); i++) {
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
    public int getSizeInventory() {
        return filters.length;
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int index) {
        return filters[index];
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = getStackInSlot(index);
        if (stack != null) {
            if (stack.stackSize > count) {
                stack = stack.splitStack(count);
                markDirty();
            } else {
                setInventorySlotContents(index, null);
            }
        }
        return null;  // ghost items
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index) {
        filters[index] = null;
        markDirty();
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        filters[index] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
		for (int i = 0; i < getSizeInventory(); ++i) {
			if (getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0) {
				filters[i] = null;
			}
		}
        writeToNBT(moduleItem.getTagCompound());
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;  // anything can go in a filter
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(name);
    }
}
