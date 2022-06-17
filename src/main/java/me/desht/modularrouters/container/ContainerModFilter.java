package me.desht.modularrouters.container;

import me.desht.modularrouters.container.handler.GhostItemHandler;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;
import static me.desht.modularrouters.container.Layout.SLOT_Y_SPACING;

public class ContainerModFilter extends ContainerSmartFilter {
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 162;
    private static final int PLAYER_HOTBAR_Y = 220;

    public final IItemHandlerModifiable handler;

    public ContainerModFilter(int windowId, Inventory invPlayer, FriendlyByteBuf extraData) {
        this(windowId, invPlayer, MFLocator.fromBuffer(extraData));
    }

    public ContainerModFilter(int windowId, Inventory invPlayer, MFLocator locator) {
        super(ModContainerTypes.CONTAINER_MOD_FILTER.get(), windowId, invPlayer, locator);

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
        ItemStack stack;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            stack.setCount(1);

            if (index == 0) {
                // shift-clicking in the ghost slot: clear it from the filter
                slot.set(ItemStack.EMPTY);
            } else if (index >= 1) {
                // shift-clicking in player inventory: copy it into the ghost slot
                // but don't remove it from playerpack inventory
                Slot s = slots.get(0);
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
                // normal left-click
                if (slot == 0) {
                    Slot s = slots.get(slot);
                    if (!getCarried().isEmpty()) {
                        ItemStack stack1 = getCarried().copy();
                        stack1.setCount(1);
                        s.set(stack1);
                    } else {
                        s.set(ItemStack.EMPTY);
                    }
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
