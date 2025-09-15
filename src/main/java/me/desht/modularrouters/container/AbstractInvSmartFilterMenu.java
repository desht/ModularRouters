package me.desht.modularrouters.container;

import me.desht.modularrouters.container.handler.GhostItemHandler;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;
import static me.desht.modularrouters.container.Layout.SLOT_Y_SPACING;

public abstract class AbstractInvSmartFilterMenu extends AbstractSmartFilterMenu {
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 162;
    private static final int PLAYER_HOTBAR_Y = 220;

    public final IItemHandlerModifiable handler;

    protected AbstractInvSmartFilterMenu(MenuType<?> menuType, int windowId, Inventory invPlayer, MFLocator locator) {
        super(menuType, windowId, invPlayer, locator);

        handler = new GhostItemHandler(1);

        // slot for the ghost item
        addSlot(new FilterSlot(handler, 0, 7, 19));

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

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            ItemStack stack = stackInSlot.copyWithCount(1);

            if (index == 0) {
                // shift-clicking in the ghost slot: clear it from the filter
                slot.set(ItemStack.EMPTY);
            } else if (index >= 1) {
                // shift-clicking in player inventory: copy it into the ghost slot
                // but don't remove it from player inventory
                Slot s = slots.getFirst();
                s.set(stack);
                slot.set(stackInSlot);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slot, int dragType, ClickType clickTypeIn, Player player) {
        switch (clickTypeIn) {
            case PICKUP:
            case QUICK_CRAFT:
                // normal left-click
                if (slot == 0) {
                    slots.get(slot).set(getCarried().isEmpty() ? ItemStack.EMPTY : getCarried().copyWithCount(1));
                    return;
                }
            case THROW:
                if (slot == 0) {
                    return;
                }
        }
        super.clicked(slot, dragType, clickTypeIn, player);
    }
}
