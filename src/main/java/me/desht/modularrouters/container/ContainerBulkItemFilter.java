package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.FilterHandler.BulkFilterHandler;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;
import static me.desht.modularrouters.container.Layout.SLOT_Y_SPACING;

public class ContainerBulkItemFilter extends ContainerSmartFilter {
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 151;
    private static final int PLAYER_HOTBAR_Y = 209;

    private final FilterHandler handler;

    public ContainerBulkItemFilter(EntityPlayer player, ItemStack filterStack, EnumHand hand, TileEntityItemRouter router) {
        super(player, filterStack, hand, router);

        BulkItemFilter.checkAndMigrateOldNBT(filterStack);
        handler = new BulkFilterHandler(filterStack);

        // slots for the (ghost) filter items
        for (int i = 0; i < handler.getSlots(); i++) {
            FilterSlot slot = router == null ?
                    new FilterSlot(handler, player, hand, i, 8 + SLOT_X_SPACING * (i % 9), 19 + SLOT_Y_SPACING * (i / 9)) :
                    new FilterSlot(handler, router, i, 8 + SLOT_X_SPACING * (i % 9), 19 + SLOT_Y_SPACING * (i / 9));
            addSlotToContainer(slot);
        }

        // player's main inventory - uses default locations for standard inventory texture file
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, PLAYER_INV_X + j * SLOT_X_SPACING, PLAYER_INV_Y + i * SLOT_Y_SPACING));
            }
        }

        // player's hotbar - uses default locations for standard action bar texture file
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(player.inventory, i, PLAYER_INV_X + i * SLOT_X_SPACING, PLAYER_HOTBAR_Y));
        }
    }

    public void clearSlots() {
        for (int i = 0; i < handler.getSlots(); i++) {
            handler.setStackInSlot(i, null);
        }
        handler.save();

        if (getRouter() != null && !getRouter().getWorld().isRemote) {
            getRouter().recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
        }
    }

    public int mergeInventory(IItemHandler srcInv, Filter.Flags flags, boolean clearFirst) {
        if (srcInv == null) {
            return 0;
        }
        SetofItemStack stacks = clearFirst ? new SetofItemStack(flags) : SetofItemStack.fromItemHandler(handler, flags);
        int origSize = stacks.size();

        for (int i = 0; i < srcInv.getSlots() && stacks.size() < handler.getSlots(); i++) {
            ItemStack stack = srcInv.getStackInSlot(i);
            if (stack != null) {
                ItemStack stack1 = stack.copy();
                stack1.stackSize = 1;
                stacks.add(stack1);
            }
        }

        int slot = 0;
        for (ItemStack stack : stacks.sortedList()) {
            handler.setStackInSlot(slot++, stack);
        }
        while (slot < handler.getSlots()) {
            handler.setStackInSlot(slot++, null);
        }
        handler.save();

        if (getRouter() != null && !getRouter().getWorld().isRemote) {
            getRouter().recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
        }

        return stacks.size() - origSize;
    }

    @Nullable
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack stack;
        Slot srcSlot = inventorySlots.get(index);

        if (srcSlot != null && srcSlot.getHasStack()) {
            ItemStack stackInSlot = srcSlot.getStack();
            stack = stackInSlot.copy();
            stack.stackSize = 1;

            if (index < handler.getSlots()) {
                // shift-clicking in a filter slot: clear it from the filter
                srcSlot.putStack(null);
            } else if (index >= handler.getSlots()) {
                // shift-clicking in player inventory: copy it into the filter (if not already present)
                // but don't remove it from player inventory
                int freeSlot;
                for (freeSlot = 0; freeSlot < handler.getSlots(); freeSlot++) {
                    ItemStack stack0 = handler.getStackInSlot(freeSlot);
                    if (stack0 == null || stack0.stackSize == 0 || ItemStack.areItemStacksEqual(stack0, stack)) {
                        break;
                    }
                }
                if (freeSlot < handler.getSlots()) {
                    inventorySlots.get(freeSlot).putStack(stack);
                    srcSlot.putStack(stackInSlot);
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public ItemStack slotClick(int slot, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        switch (clickTypeIn) {
            case PICKUP:
                // normal left-click
                if (slot < handler.getSlots() && slot >= 0) {
                    Slot s = inventorySlots.get(slot);
                    ItemStack stackOnCursor = player.inventory.getItemStack();
                    if (stackOnCursor != null) {
                        ItemStack stack1 = stackOnCursor.copy();
                        stack1.stackSize = 1;
                        s.putStack(stack1);
                    } else {
                        s.putStack(null);
                    }
                    return null;
                }
            case THROW:
                if (slot < handler.getSlots() && slot >= 0) {
                    return null;
                }
        }
        return super.slotClick(slot, dragType, clickTypeIn, player);
    }
}
