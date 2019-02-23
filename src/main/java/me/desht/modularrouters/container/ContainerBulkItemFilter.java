package me.desht.modularrouters.container;

import com.google.common.collect.Sets;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler.BulkFilterHandler;
import me.desht.modularrouters.container.slot.BaseModuleSlot.BulkFilterSlot;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.HashableItemStackWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.IItemHandler;

import java.util.Set;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;
import static me.desht.modularrouters.container.Layout.SLOT_Y_SPACING;

public class ContainerBulkItemFilter extends ContainerSmartFilter {
    private static final int INV_START = BulkItemFilter.FILTER_SIZE;
    private static final int INV_END = INV_START + 26;
    private static final int HOTBAR_START = INV_END + 1;
    private static final int HOTBAR_END = HOTBAR_START + 8;

    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 151;
    private static final int PLAYER_HOTBAR_Y = 209;

    private final int currentSlot;  // currently-selected slot for player
    private final TileEntityItemRouter router;
    private final BulkFilterHandler handler;

    public ContainerBulkItemFilter(EntityPlayer player, EnumHand hand, TileEntityItemRouter router) {
        super(player, hand, router);

        this.handler = new BulkFilterHandler(filterStack);
        this.currentSlot = player.inventory.currentItem + HOTBAR_START;
        this.router = router;

        // slots for the (ghost) filter items
        for (int i = 0; i < handler.getSlots(); i++) {
            BulkFilterSlot slot = router == null ?
                    new BulkFilterSlot(handler, player, hand, i, 8 + SLOT_X_SPACING * (i % 9), 19 + SLOT_Y_SPACING * (i / 9)) :
                    new BulkFilterSlot(handler, router, i, 8 + SLOT_X_SPACING * (i % 9), 19 + SLOT_Y_SPACING * (i / 9));
            addSlot(slot);
        }

        // player's main inventory - uses default locations for standard inventory texture file
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(player.inventory, j + i * 9 + 9, PLAYER_INV_X + j * SLOT_X_SPACING, PLAYER_INV_Y + i * SLOT_Y_SPACING));
            }
        }

        // player's hotbar - uses default locations for standard action bar texture file
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(player.inventory, i, PLAYER_INV_X + i * SLOT_X_SPACING, PLAYER_HOTBAR_Y));
        }
    }

    public void clearSlots() {
        for (int i = 0; i < handler.getSlots(); i++) {
            handler.setStackInSlot(i, ItemStack.EMPTY);
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
        Set<HashableItemStackWrapper> stacks = clearFirst ? Sets.newHashSet() : HashableItemStackWrapper.makeSet(handler, flags);
//        SetofItemStack stacks = clearFirst ? new SetofItemStack(flags) : SetofItemStack.fromItemHandler(handler, flags);
        int origSize = stacks.size();

        for (int i = 0; i < srcInv.getSlots() && stacks.size() < handler.getSlots(); i++) {
            ItemStack stack = srcInv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack stack1 = stack.copy();
                stack1.setCount(1);
                stacks.add(new HashableItemStackWrapper(stack1, flags));
            }
        }

        int slot = 0;
        for (ItemStack stack : HashableItemStackWrapper.sortedList(stacks)) {
            handler.setStackInSlot(slot++, stack);
        }
        while (slot < handler.getSlots()) {
            handler.setStackInSlot(slot++, ItemStack.EMPTY);
        }
        handler.save();

        if (getRouter() != null && !getRouter().getWorld().isRemote) {
            getRouter().recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
        }

        return stacks.size() - origSize;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack stack;
        Slot srcSlot = inventorySlots.get(index);

        if (srcSlot != null && srcSlot.getHasStack()) {
            ItemStack stackInSlot = srcSlot.getStack();
            stack = stackInSlot.copy();
            stack.setCount(1);

            if (index < handler.getSlots()) {
                // shift-clicking in a filter slot: clear it from the filter
                srcSlot.putStack(ItemStack.EMPTY);
            } else if (index >= handler.getSlots()) {
                // shift-clicking in player inventory: copy it into the filter (if not already present)
                // but don't remove it from player inventory
                int freeSlot;
                for (freeSlot = 0; freeSlot < handler.getSlots(); freeSlot++) {
                    ItemStack stack0 = handler.getStackInSlot(freeSlot);
                    if (stack0.isEmpty() || ItemStack.areItemStacksEqual(stack0, stack)) {
                        break;
                    }
                }
                if (freeSlot < handler.getSlots()) {
                    inventorySlots.get(freeSlot).putStack(stack);
                    srcSlot.putStack(stackInSlot);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack slotClick(int slot, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        switch (clickTypeIn) {
            case PICKUP:
                // normal left-click
                if (router == null && slot == currentSlot) {
                    // no messing with the module that triggered this container's creation
                    return ItemStack.EMPTY;
                }
                if (slot < handler.getSlots() && slot >= 0) {
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
                if (slot < handler.getSlots() && slot >= 0) {
                    return ItemStack.EMPTY;
                }
        }
        return super.slotClick(slot, dragType, clickTypeIn, player);
    }
}
