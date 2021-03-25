package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler.BulkFilterHandler;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

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
    private final BulkFilterHandler handler;

    public ContainerBulkItemFilter(int windowId, PlayerInventory invPlayer, PacketBuffer extraData) {
        this(windowId, invPlayer, MFLocator.fromBuffer(extraData));
    }

    public ContainerBulkItemFilter(int windowId, PlayerInventory invPlayer, MFLocator loc) {
        super(ModContainerTypes.CONTAINER_BULK_ITEM_FILTER.get(), windowId, invPlayer, loc);

        this.handler = new BulkFilterHandler(filterStack, router);
        this.currentSlot = invPlayer.selected + HOTBAR_START;

        // slots for the (ghost) filter items
        for (int i = 0; i < handler.getSlots(); i++) {
            addSlot(new FilterSlot(handler, i, 8 + SLOT_X_SPACING * (i % 9), 19 + SLOT_Y_SPACING * (i / 9)));
        }

        // player's main inventory - uses default locations for standard inventory texture file
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(invPlayer, j + i * 9 + 9, PLAYER_INV_X + j * SLOT_X_SPACING, PLAYER_INV_Y + i * SLOT_Y_SPACING));
            }
        }

        // player's hotbar - uses default locations for standard action bar texture file
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(invPlayer, i, PLAYER_INV_X + i * SLOT_X_SPACING, PLAYER_HOTBAR_Y));
        }
    }

    public void clearSlots() {
        for (int i = 0; i < handler.getSlots(); i++) {
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
        handler.save();

        if (getRouter() != null && !getRouter().getLevel().isClientSide) {
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
            if (!stack.isEmpty()) {
                stacks.add(ItemHandlerHelper.copyStackWithSize(stack, 1));
            }
        }

        int slot = 0;
        for (ItemStack stack : stacks.sortedList()) {
            handler.setStackInSlot(slot++, stack);
        }
        while (slot < handler.getSlots()) {
            handler.setStackInSlot(slot++, ItemStack.EMPTY);
        }
        handler.save();

        if (getRouter() != null && !getRouter().getLevel().isClientSide) {
            getRouter().recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
        }

        return stacks.size() - origSize;
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        ItemStack stack;
        Slot srcSlot = slots.get(index);

        if (srcSlot != null && srcSlot.hasItem()) {
            ItemStack stackInSlot = srcSlot.getItem();
            stack = stackInSlot.copy();
            stack.setCount(1);

            if (index < handler.getSlots()) {
                // shift-clicking in a filter slot: clear it from the filter
                srcSlot.set(ItemStack.EMPTY);
            } else if (index >= handler.getSlots()) {
                // shift-clicking in player inventory: copy it into the filter (if not already present)
                // but don't remove it from player inventory
                int freeSlot;
                for (freeSlot = 0; freeSlot < handler.getSlots(); freeSlot++) {
                    ItemStack stack0 = handler.getStackInSlot(freeSlot);
                    if (stack0.isEmpty() || ItemStack.matches(stack0, stack)) {
                        break;
                    }
                }
                if (freeSlot < handler.getSlots()) {
                    slots.get(freeSlot).set(stack);
                    srcSlot.set(stackInSlot);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack clicked(int slot, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (clickTypeIn == ClickType.PICKUP) {// normal left-click
            if (router == null && slot == currentSlot) {
                // no messing with the module that triggered this container's creation
                return ItemStack.EMPTY;
            }
            if (slot < handler.getSlots() && slot >= 0) {
                Slot s = slots.get(slot);
                ItemStack stackOnCursor = player.inventory.getCarried();
                if (!stackOnCursor.isEmpty()) {
                    ItemStack stack1 = stackOnCursor.copy();
                    stack1.setCount(1);
                    s.set(stack1);
                } else {
                    s.set(ItemStack.EMPTY);
                }
                return ItemStack.EMPTY;
            }
        }
        if (slot < handler.getSlots() && slot >= 0) {
            // allow nothing else!
            return ItemStack.EMPTY;
        }
        return super.clicked(slot, dragType, clickTypeIn, player);
    }
}
