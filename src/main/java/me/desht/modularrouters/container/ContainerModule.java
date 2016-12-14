package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.FilterHandler.ModuleFilterHandler;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;
import static me.desht.modularrouters.container.Layout.SLOT_Y_SPACING;

public class ContainerModule extends Container {
    private static final int INV_START = Filter.FILTER_SIZE;
    private static final int INV_END = INV_START + 26;
    private static final int HOTBAR_START = INV_END + 1;
    private static final int HOTBAR_END = HOTBAR_START + 8;

    private static final int PLAYER_INV_Y = 100;
    private static final int PLAYER_INV_X = 16;
    private static final int PLAYER_HOTBAR_Y = PLAYER_INV_Y + 58;

    public final FilterHandler filterHandler;
    private final int currentSlot;  // currently-selected slot for player
    private final TileEntityItemRouter router;

    public ContainerModule(EntityPlayer player, EnumHand hand, ItemStack moduleStack) {
        this(player, hand, moduleStack, null);
    }

    public ContainerModule(EntityPlayer player, EnumHand hand, ItemStack moduleStack, TileEntityItemRouter router) {
        this.filterHandler = new ModuleFilterHandler(moduleStack);
        this.currentSlot = player.inventory.currentItem + HOTBAR_START;
        this.router = router;  // null if module is in player's hand

        // slots for the (ghost) filter items
        for (int i = 0; i < Filter.FILTER_SIZE; i++) {
            FilterSlot slot = router == null ?
                    new FilterSlot(filterHandler, player, hand, i, 8 + SLOT_X_SPACING * (i % 3), 17 + SLOT_Y_SPACING * (i / 3)) :
                    new FilterSlot(filterHandler, router, i, 8 + SLOT_X_SPACING * (i % 3), 17 + SLOT_Y_SPACING * (i / 3));
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

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack stack;
        Slot srcSlot = inventorySlots.get(index);

        if (srcSlot != null && srcSlot.getHasStack()) {
            ItemStack stackInSlot = srcSlot.getStack();
            stack = stackInSlot.copy();
            stack.setCount(1);

            if (index < Filter.FILTER_SIZE) {
                // shift-clicking in a filter slot: clear it from the filter
                srcSlot.putStack(ItemStack.EMPTY);
            } else if (index >= INV_START) {
                // shift-clicking in player inventory: copy it into the filter (if not already present)
                // but don't remove it from player inventory
                int freeSlot;
                for (freeSlot = 0; freeSlot < Filter.FILTER_SIZE; freeSlot++) {
                    ItemStack stack0 = filterHandler.getStackInSlot(freeSlot);
                    if (stack0.isEmpty() || ItemStack.areItemStacksEqual(stack0, stack)) {
                        break;
                    }
                }
                if (freeSlot < Filter.FILTER_SIZE) {
                    inventorySlots.get(freeSlot).putStack(stack);
                    srcSlot.putStack(stackInSlot);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack slotClick(int slot, int dragType, ClickType clickTypeIn, EntityPlayer player) {
//        System.out.println("slotClick: slot=" + slot + ", dragtype=" + dragType + ", clicktype=" + clickTypeIn);

        switch (clickTypeIn) {
            case PICKUP:
                // normal left-click
                if (router == null && slot == currentSlot) {
                    // no messing with the module that triggered this container's creation
                    return ItemStack.EMPTY;
                }
                if (slot < Filter.FILTER_SIZE && slot >= 0) {
                    Slot s = inventorySlots.get(slot);
                    ItemStack stackOnCursor = player.inventory.getItemStack();
                    if (!stackOnCursor.isEmpty()) {
                        ItemStack stack1 = stackOnCursor.copy();
                        stack1.setCount(1);
                        s.putStack(stack1);
                    } else {
                        s.putStack(ItemStack.EMPTY);
                    }
                    return ItemStack.EMPTY;
                }
            case THROW:
                if (slot < Filter.FILTER_SIZE && slot >= 0) {
                    return ItemStack.EMPTY;
                }
        }
        return super.slotClick(slot, dragType, clickTypeIn, player);
    }
}
