package me.desht.modularrouters.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import static me.desht.modularrouters.container.Layout.*;

public class ModuleContainer extends Container {

    private static final int INV_START = ModuleInventory.N_FILTER_SLOTS, INV_END = INV_START+26,
            HOTBAR_START = INV_END+1, HOTBAR_END = HOTBAR_START+8;

    public final ModuleInventory inventory;

    public ModuleContainer(EntityPlayer player, ModuleInventory inventory) {
        this.inventory = inventory;

        // slots for the (ghost) filter items
        for (int i = 0; i < ModuleInventory.N_FILTER_SLOTS; i++) {
            addSlotToContainer(new Slot(inventory, i, 8 + SLOT_X_SPACING * (i % 3), 17 + SLOT_Y_SPACING * (i / 3)));
        }

        // player's main inventory - uses default locations for standard inventory texture file
        for (int i = 0; i < 3; i++)	{
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * SLOT_X_SPACING, 84 + i * SLOT_Y_SPACING));
            }
        }

        // player's hotbar - uses default locations for standard action bar texture file
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * SLOT_X_SPACING, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack stack;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            stack.stackSize = 1;

            if (index < ModuleInventory.N_FILTER_SLOTS) {
                // shift-clicking in a filter slot: clear it from the filter
                slot.putStack(null);
            } else if (index >= INV_START) {
                System.out.println("transfer into filter");
                // shift-clicking in player inventory: copy it into the filter (if not already present)
                // but don't remove it from player inventory
                int freeSlot;
                for (freeSlot = 0; freeSlot < ModuleInventory.N_FILTER_SLOTS; freeSlot++) {
                    ItemStack stack0 = inventory.getStackInSlot(freeSlot);
                    if (stack0 == null || stack0.stackSize == 0 || stack0.isItemEqual(stack)) {
                        break;
                    }
                }
                if (freeSlot < ModuleInventory.N_FILTER_SLOTS) {
                    Slot s = inventorySlots.get(freeSlot);
                    s.putStack(stack);
                    slot.putStack(stackInSlot);
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack slotClick(int slot, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        System.out.println("slotClick: slot=" + slot + " dragtype=" + dragType + " clicktype=" + clickTypeIn);

        if (dragType > 0 && slot < ModuleInventory.N_FILTER_SLOTS && slot >= 0) {
            return null;
        }

        switch (clickTypeIn) {
            case PICKUP:
                // normal left-click
                if (slot < ModuleInventory.N_FILTER_SLOTS && slot >= 0) {
                    Slot s = inventorySlots.get(slot);
                    if (player.inventory.getItemStack() != null) {
                        ItemStack stack1 = player.inventory.getItemStack().copy();
                        stack1.stackSize = 1;
                        s.putStack(stack1);
                    } else {
                        s.putStack(null);
                    }
                    return null;
                }
            case THROW:
                if (slot < ModuleInventory.N_FILTER_SLOTS && slot >= 0) {
                    return null;
                }
        }
        return super.slotClick(slot, dragType, clickTypeIn, player);
    }

    /**
     * Vanilla mergeItemStack method doesn't correctly handle inventories whose
     * max stack size is 1 when you shift-click into the inventory.
     * This is a modified method I wrote to handle such cases.
     * Note you only need it if your slot / inventory's max stack size is 1
     */
    @Override
    protected boolean mergeItemStack(ItemStack stack, int start, int end, boolean backwards)
    {
        boolean flag1 = false;
        int k = (backwards ? end - 1 : start);
        Slot slot;
        ItemStack itemstack1;

        if (stack.isStackable())
        {
            while (stack.stackSize > 0 && (!backwards && k < end || backwards && k >= start))
            {
                slot = inventorySlots.get(k);
                itemstack1 = slot.getStack();

                if (!slot.isItemValid(stack)) {
                    k += (backwards ? -1 : 1);
                    continue;
                }

                if (itemstack1 != null && itemstack1.getItem() == stack.getItem() &&
                        (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, itemstack1))
                {
                    int l = itemstack1.stackSize + stack.stackSize;

                    if (l <= stack.getMaxStackSize() && l <= slot.getSlotStackLimit()) {
                        stack.stackSize = 0;
                        itemstack1.stackSize = l;
                        inventory.markDirty();
                        flag1 = true;
                    } else if (itemstack1.stackSize < stack.getMaxStackSize() && l < slot.getSlotStackLimit()) {
                        stack.stackSize -= stack.getMaxStackSize() - itemstack1.stackSize;
                        itemstack1.stackSize = stack.getMaxStackSize();
                        inventory.markDirty();
                        flag1 = true;
                    }
                }

                k += (backwards ? -1 : 1);
            }
        }
        if (stack.stackSize > 0)
        {
            k = (backwards ? end - 1 : start);
            while (!backwards && k < end || backwards && k >= start) {
                slot = inventorySlots.get(k);
                itemstack1 = slot.getStack();

                if (!slot.isItemValid(stack)) {
                    k += (backwards ? -1 : 1);
                    continue;
                }

                if (itemstack1 == null) {
                    int l = stack.stackSize;
                    if (l <= slot.getSlotStackLimit()) {
                        slot.putStack(stack.copy());
                        stack.stackSize = 0;
                        inventory.markDirty();
                        flag1 = true;
                        break;
                    } else {
                        putStackInSlot(k, new ItemStack(stack.getItem(), slot.getSlotStackLimit(), stack.getItemDamage()));
                        stack.stackSize -= slot.getSlotStackLimit();
                        inventory.markDirty();
                        flag1 = true;
                    }
                }

                k += (backwards ? -1 : 1);
            }
        }

        return flag1;
    }
}
