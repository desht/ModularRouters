package me.desht.modularrouters.container;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public abstract class ContainerMRBase extends Container {
    ContainerMRBase(@Nullable ContainerType<?> type, int id) {
        super(type, id);
    }

    /*
     * Modified mergeItemStack to fix a vanilla problem: when items are shift-clicked into a slot which already has
     * some of that item, putStack() isn't called, rather the itemstack size is simply updated.  This means
     * ItemStackHandler#onContentsChanged() never gets called in this case, which is no good for us.
     * Also, modified to properly honour slot/stack limits (important for upgrade limits)
     */
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();
                if (!itemstack.isEmpty() && areItemsAndTagsEqual(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    // modified HERE
                    int maxSize = Math.min(slot.getItemStackLimit(itemstack), Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize()));
//                    int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.putStack(itemstack);   // <- modified HERE
//                        slot.onSlotChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.putStack(itemstack);   // <- modified HERE
//                        slot.onSlotChanged();
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while(true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot1 = this.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();
                if (itemstack1.isEmpty() && slot1.isItemValid(stack)) {
                    // modified HERE
                    int limit = Math.min(slot1.getSlotStackLimit(), slot1.getItemStackLimit(stack));
                    if (stack.getCount() > limit) {
                        slot1.putStack(stack.split(limit));
                    } else {
                        slot1.putStack(stack.split(stack.getCount()));
                    }
//                    if (stack.getCount() > slot1.getSlotStackLimit()) {
//                        slot1.putStack(stack.split(slot1.getSlotStackLimit()));
//                    } else {
//                        slot1.putStack(stack.split(stack.getCount()));
//                    }

                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }
}
