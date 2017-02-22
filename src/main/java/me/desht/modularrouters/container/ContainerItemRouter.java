package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.slot.BufferSlot;
import me.desht.modularrouters.container.slot.ValidatingSlot;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;
import static me.desht.modularrouters.container.Layout.SLOT_Y_SPACING;

public class ContainerItemRouter extends Container {
    public static final int BUFFER_SLOT = 0;
    public static final int MODULE_SLOT_START = 1;
    public static final int MODULE_SLOT_END = 9;
    public static final int UPGRADE_SLOT_START = 10;
    public static final int UPGRADE_SLOT_END = 14;
    private static final int BUFFER_XPOS = 8;
    private static final int BUFFER_YPOS = 40;
    private static final int HOTBAR_XPOS = 8;
    private static final int HOTBAR_YPOS = 162;
    private static final int PLAYER_INVENTORY_XPOS = 8;
    private static final int PLAYER_INVENTORY_YPOS = 104;
    public static final int MODULE_XPOS = 8;
    private static final int MODULE_YPOS = 72;
    public static final int UPGRADE_XPOS = 80;
    private static final int UPGRADE_YPOS = 40;
    public static final int TE_FIRST_SLOT = 36;
    private static final int TE_LAST_SLOT = TE_FIRST_SLOT + UPGRADE_SLOT_END;

    private final TileEntityItemRouter router;

    public ContainerItemRouter(InventoryPlayer invPlayer, TileEntityItemRouter router) {
        this.router = router;

        // player's hotbar
        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(invPlayer, x, HOTBAR_XPOS + SLOT_X_SPACING * x, HOTBAR_YPOS));
        }
        // player's main inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                int slotNumber = 9 + y * 9 + x;
                int xpos = PLAYER_INVENTORY_XPOS + x * SLOT_X_SPACING;
                int ypos = PLAYER_INVENTORY_YPOS + y * SLOT_Y_SPACING;
                addSlotToContainer(new Slot(invPlayer, slotNumber, xpos, ypos));
            }
        }

        // item router buffer
        addSlotToContainer(new BufferSlot(router, BUFFER_SLOT, BUFFER_XPOS, BUFFER_YPOS));

        // item router modules
        for (int slot = 0; slot < TileEntityItemRouter.N_MODULE_SLOTS; slot++) {
            addSlotToContainer(new ValidatingSlot.Module(router, slot, MODULE_XPOS + slot * SLOT_X_SPACING, MODULE_YPOS));
        }
        // item router upgrades
        for (int slot = 0; slot < TileEntityItemRouter.N_UPGRADE_SLOTS; slot++) {
            addSlotToContainer(new ValidatingSlot.Upgrade(router, slot, UPGRADE_XPOS + slot * SLOT_X_SPACING, UPGRADE_YPOS));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return router.getWorld().getTileEntity(router.getPos()) == router
                && player.getDistanceSq(router.getPos().add(0.5, 0.5, 0.5)) <= 64;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int sourceSlotIndex) {
        Slot sourceSlot = inventorySlots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (sourceSlotIndex >= 0 && sourceSlotIndex < TE_FIRST_SLOT) {
            // This is a vanilla container slot so merge the stack into the right part of the tile inventory
            if (sourceStack.getItem() instanceof ItemModule) {
                // shift-clicked a module: see if there's a free module slot
                if (!mergeItemStack(sourceStack, TE_FIRST_SLOT + MODULE_SLOT_START, TE_FIRST_SLOT + MODULE_SLOT_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (sourceStack.getItem() instanceof ItemUpgrade) {
                if (!mergeItemStack(sourceStack, TE_FIRST_SLOT + UPGRADE_SLOT_START, TE_FIRST_SLOT + UPGRADE_SLOT_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!mergeItemStack(sourceStack, TE_FIRST_SLOT + BUFFER_SLOT, TE_FIRST_SLOT + BUFFER_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (sourceSlotIndex >= TE_FIRST_SLOT && sourceSlotIndex < TE_FIRST_SLOT + TE_LAST_SLOT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!mergeItemStack(sourceStack, 0, TE_FIRST_SLOT - 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.err.print("Invalid moduleSlotIndex: " + sourceSlotIndex);
            return ItemStack.EMPTY;
        }

        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.isEmpty()) {
            sourceSlot.putStack(ItemStack.EMPTY);
        } else {
            sourceSlot.onSlotChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }
}
