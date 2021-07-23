package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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
    protected final ModularRouterBlockEntity router;
    private final MFLocator locator;

    public ContainerModule(int windowId, Inventory inv, FriendlyByteBuf extra) {
        this(ModContainerTypes.CONTAINER_MODULE_BASIC.get(), windowId, inv, MFLocator.fromBuffer(extra));
    }

    public ContainerModule(MenuType type, int windowId, Inventory inv, FriendlyByteBuf extra) {
        this(type, windowId, inv, MFLocator.fromBuffer(extra));
    }

    public ContainerModule(MenuType type, int windowId, Inventory inv, MFLocator locator) {
        super(type, windowId);

        this.locator = locator;
        this.router = locator.getRouter(inv.player.level).orElse(null);
        assert router != null || locator.hand != null;

        ItemStack moduleStack = locator.getModuleStack(inv.player);
        this.filterHandler = new ModuleFilterHandler(moduleStack, router);
        this.augmentHandler = new AugmentHandler(moduleStack, router);
        this.currentSlot = inv.selected + HOTBAR_START;

        // slots for the (ghost) filter items
        for (int i = 0; i < Filter.FILTER_SIZE; i++) {
            addSlot(new FilterSlot(filterHandler, i, 8 + SLOT_X_SPACING * (i % 3), 17 + SLOT_Y_SPACING * (i / 3)));
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

    public ModularRouterBlockEntity getRouter() {
        return router;
    }

    protected void transferStackInExtraSlot(Player player, int index) {
        // does nothing by default, to be overridden
    }

    protected ItemStack slotClickExtraSlot(int slot, int dragType, ClickType clickTypeIn, Player player) {
        // does nothing by default, to be overridden
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return router == null || !router.isRemoved();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot srcSlot = slots.get(index);

        if (srcSlot != null && srcSlot.hasItem()) {
            if (index < AUGMENT_START) {
                // shift-clicking in a filter slot: clear it from the filter
                srcSlot.set(ItemStack.EMPTY);
            } else if (index < AUGMENT_START + ItemAugment.SLOTS) {
                // shift-clicking in augment slots
                ItemStack stackInSlot = srcSlot.getItem();
                if (!moveItemStackTo(stackInSlot, INV_START, HOTBAR_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
                srcSlot.set(stackInSlot);
                broadcastChanges();
            } else if (index <= HOTBAR_END) {
                // shift-clicking in player inventory
                ItemStack stackInSlot = srcSlot.getItem();
                if (stackInSlot.getItem() instanceof ItemAugment && augmentHandler.getHolderStack().getCount() == 1) {
                    // copy augment items into one of the augment slots if possible
                    if (!moveItemStackTo(stackInSlot, AUGMENT_START, AUGMENT_START + ItemAugment.SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                    broadcastChanges();
                } else {
                    // copy it into the filter (if not already present)
                    // but don't remove it from player inventory
                    ItemStack stack = stackInSlot.copy();
                    stack.setCount(1);
                    int i;
                    int firstFree = -1;
                    for (i = 0; i < Filter.FILTER_SIZE; i++) {
                        ItemStack stack0 = filterHandler.getStackInSlot(i);
                        if (ItemStack.isSame(stack0, stack)) {
                            firstFree = i;
                            break;
                        } else if (firstFree < 0 && stack0.isEmpty() && filterHandler.isItemValid(i, stack)) {
                            firstFree = i;
                        }
                    }
                    if (firstFree >= 0) {
                        slots.get(firstFree).set(stack);
                        srcSlot.set(stackInSlot);
                    }
                }
            } else {
                transferStackInExtraSlot(player, index);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slot, int dragType, ClickType clickTypeIn, Player player) {
        boolean forceUpdate = false;

        if (slot > HOTBAR_END) {
            slotClickExtraSlot(slot, dragType, clickTypeIn, player);
            return;
        }
        if (slot >= AUGMENT_START && slot < AUGMENT_START + ItemAugment.SLOTS && augmentHandler.getHolderStack().getCount() > 1) {
            // prevent augment dupe
            return;
        }

        switch (clickTypeIn) {
            case PICKUP:
                // normal left-click
                if (router == null && slot == currentSlot) {
                    // no messing with the module that triggered this container's creation
                    return;
                }
                if (slot >= 0 && slot < Filter.FILTER_SIZE) {
                    Slot s = slots.get(slot);
                    ItemStack stackOnCursor = getCarried();
                    if (stackOnCursor.isEmpty() || isItemOKForFilter(stackOnCursor, slot)) {
                        s.set(stackOnCursor.isEmpty() ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(stackOnCursor, 1));
                    }
                    return;
                } else if (slot >= AUGMENT_START && slot < AUGMENT_START + ItemAugment.SLOTS && augmentHandler.getHolderStack().getCount() == 1) {
                    forceUpdate = true;
                }
            case THROW:
                if (slot >= 0 && slot < Filter.FILTER_SIZE) {
                    return;
                } else if (slot >= AUGMENT_START && slot < AUGMENT_START + ItemAugment.SLOTS && augmentHandler.getHolderStack().getCount() == 1) {
                    forceUpdate = true;
                }
        }
        super.clicked(slot, dragType, clickTypeIn, player);
        if (forceUpdate) {
            // force item handler's onContentsChanged() to be called
            slots.get(slot).set(slots.get(slot).getItem());
            broadcastChanges();
        }
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
    public boolean canDragTo(Slot p_94531_1_) {
        return false;
    }
}
