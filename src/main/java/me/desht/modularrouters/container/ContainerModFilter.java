package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.GhostItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;
import static me.desht.modularrouters.container.Layout.SLOT_Y_SPACING;

public class ContainerModFilter extends ContainerSmartFilter {
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 162;
    private static final int PLAYER_HOTBAR_Y = 220;

    public final IItemHandlerModifiable handler;

    public ContainerModFilter(EntityPlayer player, ItemStack filterStack, EnumHand hand, TileEntityItemRouter router) {
        super(player, filterStack, hand, router);

        handler = new GhostItemHandler(1);

        // slot for the ghost item
        addSlotToContainer(new SlotItemHandler(handler, 0, 7, 19));

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

    @Nullable
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack stack;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            stack.stackSize = 1;

            if (index == 0) {
                // shift-clicking in the ghost slot: clear it from the filter
                slot.putStack(null);
            } else if (index >= 1) {
                // shift-clicking in player inventory: copy it into the ghost slot
                // but don't remove it from playerpack inventory
                Slot s = inventorySlots.get(0);
                s.putStack(stack);
                slot.putStack(stackInSlot);
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
                if (slot == 0) {
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
                if (slot == 0) {
                    return null;
                }
        }
        return super.slotClick(slot, dragType, clickTypeIn, player);
    }
}
