package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;
import static me.desht.modularrouters.container.Layout.SLOT_Y_SPACING;

public class ContainerModule extends ContainerMRBase {
    public static final int AUGMENT_START = Filter.FILTER_SIZE;
    private static final int INV_START = AUGMENT_START + ItemAugment.SLOTS;
    private static final int INV_END = INV_START + 26;
    private static final int HOTBAR_START = INV_END + 1;
    private static final int HOTBAR_END = HOTBAR_START + 8;

    private static final int PLAYER_INV_Y = 116;
    private static final int PLAYER_INV_X = 16;
    private static final int PLAYER_HOTBAR_Y = PLAYER_INV_Y + 58;

    private final ModuleFilterHandler filterHandler;
    private final AugmentHandler augmentHandler;
    private final int currentSlot;  // currently-selected slot for player
    protected final TileEntityItemRouter router;
    private final MFLocator locator;

    public ContainerModule(int windowId, PlayerInventory inv, PacketBuffer extra) {
        this(ModContainerTypes.CONTAINER_MODULE_BASIC.get(), windowId, inv, MFLocator.fromBuffer(extra));
    }

    public ContainerModule(ContainerType type, int windowId, PlayerInventory inv, PacketBuffer extra) {
        this(type, windowId, inv, MFLocator.fromBuffer(extra));
    }

    public ContainerModule(ContainerType type, int windowId, PlayerInventory inv, MFLocator locator) {
        super(type, windowId);

        this.locator = locator;
        this.router = locator.getRouter(inv.player.world).orElse(null);
        assert router != null || locator.hand != null;

        ItemStack moduleStack = locator.getModuleStack(inv.player);
        this.filterHandler = new ModuleFilterHandler(moduleStack, router);
        this.augmentHandler = new AugmentHandler(moduleStack, router);
        this.currentSlot = inv.currentItem + HOTBAR_START;

        // slots for the (ghost) filter items
        for (int i = 0; i < Filter.FILTER_SIZE; i++) {
            addSlot(new SlotItemHandler(filterHandler, i, 8 + SLOT_X_SPACING * (i % 3), 17 + SLOT_Y_SPACING * (i / 3)));
        }

        // slots for the augments
        for (int i = 0; i < ItemAugment.SLOTS; i++) {
            addSlot(new SlotItemHandler(augmentHandler, i, 78 + SLOT_X_SPACING * (i % 2), 75 + SLOT_Y_SPACING * (i / 2)));
        }

        // player's main inventory - uses default locations for standard inventory texture file
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inv, j + i * 9 + 9, PLAYER_INV_X + j * SLOT_X_SPACING, PLAYER_INV_Y + i * SLOT_Y_SPACING));
            }
        }

        // player's hotbar - uses default locations for standard action bar texture file
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inv, i, PLAYER_INV_X + i * SLOT_X_SPACING, PLAYER_HOTBAR_Y));
        }
    }

    public MFLocator getLocator() {
        return locator;
    }

    public TileEntityItemRouter getRouter() {
        return router;
    }

    protected void transferStackInExtraSlot(PlayerEntity player, int index) {
        // does nothing by default, to be overridden
    }

    protected ItemStack slotClickExtraSlot(int slot, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        // does nothing by default, to be overridden
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        Slot srcSlot = inventorySlots.get(index);

        if (srcSlot != null && srcSlot.getHasStack()) {
            if (index < AUGMENT_START) {
                // shift-clicking in a filter slot: clear it from the filter
                srcSlot.putStack(ItemStack.EMPTY);
            } else if (index < AUGMENT_START + ItemAugment.SLOTS) {
                // shift-clicking in augment slots
                ItemStack stackInSlot = srcSlot.getStack();
                if (!mergeItemStack(stackInSlot, INV_START, HOTBAR_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
                srcSlot.putStack(stackInSlot);
                detectAndSendChanges();
            } else if (index <= HOTBAR_END) {
                // shift-clicking in player inventory
                ItemStack stackInSlot = srcSlot.getStack();
                if (stackInSlot.getItem() instanceof ItemAugment && augmentHandler.getHolderStack().getCount() == 1) {
                    // copy augment items into one of the augment slots if possible
                    if (!mergeItemStack(stackInSlot, AUGMENT_START, AUGMENT_START + ItemAugment.SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                    detectAndSendChanges();
                } else {
                    // copy it into the filter (if not already present)
                    // but don't remove it from player inventory
                    ItemStack stack = stackInSlot.copy();
                    stack.setCount(1);
                    int i;
                    int firstFree = -1;
                    for (i = 0; i < Filter.FILTER_SIZE; i++) {
                        ItemStack stack0 = filterHandler.getStackInSlot(i);
                        if (ItemStack.areItemsEqual(stack0, stack)) {
                            firstFree = i;
                            break;
                        } else if (firstFree < 0 && stack0.isEmpty() && filterHandler.isItemValid(i, stack)) {
                            firstFree = i;
                        }
                    }
                    if (firstFree >= 0) {
                        inventorySlots.get(firstFree).putStack(stack);
                        srcSlot.putStack(stackInSlot);
                    }
                }
            } else {
                transferStackInExtraSlot(player, index);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack slotClick(int slot, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        boolean forceUpdate = false;

        if (slot > HOTBAR_END) {
            return slotClickExtraSlot(slot, dragType, clickTypeIn, player);
        }
        if (slot >= AUGMENT_START && slot < AUGMENT_START + ItemAugment.SLOTS && augmentHandler.getHolderStack().getCount() > 1) {
            // prevent augment dupe
            return ItemStack.EMPTY;
        }

        switch (clickTypeIn) {
            case PICKUP:
                // normal left-click
                if (router == null && slot == currentSlot) {
                    // no messing with the module that triggered this container's creation
                    return ItemStack.EMPTY;
                }
                if (slot >= 0 && slot < Filter.FILTER_SIZE) {
                    Slot s = inventorySlots.get(slot);
                    ItemStack stackOnCursor = player.inventory.getItemStack();
                    if (stackOnCursor.isEmpty() || isItemOKForFilter(stackOnCursor, slot)) {
                        s.putStack(stackOnCursor.isEmpty() ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(stackOnCursor, 1));
                    }
                    return ItemStack.EMPTY;
                } else if (slot >= AUGMENT_START && slot < AUGMENT_START + ItemAugment.SLOTS && augmentHandler.getHolderStack().getCount() == 1) {
                    forceUpdate = true;
                }
            case THROW:
                if (slot >= 0 && slot < Filter.FILTER_SIZE) {
                    return ItemStack.EMPTY;
                } else if (slot >= AUGMENT_START && slot < AUGMENT_START + ItemAugment.SLOTS && augmentHandler.getHolderStack().getCount() == 1) {
                    forceUpdate = true;
                }
        }
        ItemStack ret = super.slotClick(slot, dragType, clickTypeIn, player);
        if (forceUpdate) {
            // force item handler's onContentsChanged() to be called
            inventorySlots.get(slot).putStack(inventorySlots.get(slot).getStack());
            detectAndSendChanges();
        }
        return ret;
    }

    private boolean isItemOKForFilter(ItemStack stack, int slot) {
        if (filterHandler.isItemValid(slot, stack)) {
            for (int i = 0; i < filterHandler.getSlots(); i++) {
                if (filterHandler.getStackInSlot(i).getItem() == stack.getItem() && !(stack.getItem() instanceof ItemSmartFilter)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canDragIntoSlot(Slot p_94531_1_) {
        return false;
    }
}
