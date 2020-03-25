package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Optional;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;
import static me.desht.modularrouters.container.Layout.SLOT_Y_SPACING;

public class ContainerItemRouter extends ContainerMRBase {
    private static final int BUFFER_SLOT = 0;
    public static final int MODULE_SLOT_START = 1;
    public static final int MODULE_SLOT_END = 9;
    private static final int UPGRADE_SLOT_START = 10;
    private static final int UPGRADE_SLOT_END = 14;
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

    public ContainerItemRouter(int windowId, PlayerInventory invPlayer, PacketBuffer extraData) {
        this(windowId, invPlayer, extraData.readBlockPos());
    }

    public ContainerItemRouter(int windowId, PlayerInventory invPlayer, BlockPos routerPos) {
        super(ModContainerTypes.CONTAINER_ITEM_ROUTER.get(), windowId);

        Optional<TileEntityItemRouter> o = TileEntityItemRouter.getRouterAt(invPlayer.player.world, routerPos);
        this.router = o.orElseThrow(() -> new IllegalStateException("router missing at " + routerPos));

        // player's hotbar
        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(invPlayer, x, HOTBAR_XPOS + SLOT_X_SPACING * x, HOTBAR_YPOS));
        }
        // player's main inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                int slotNumber = 9 + y * 9 + x;
                int xpos = PLAYER_INVENTORY_XPOS + x * SLOT_X_SPACING;
                int ypos = PLAYER_INVENTORY_YPOS + y * SLOT_Y_SPACING;
                addSlot(new Slot(invPlayer, slotNumber, xpos, ypos));
            }
        }

        // item router buffer
        addSlot(new SlotItemHandler(router.getBuffer(), BUFFER_SLOT, BUFFER_XPOS, BUFFER_YPOS));

        // item router modules
        for (int slot = 0; slot < router.getModuleSlotCount(); slot++) {
            addSlot(new InstalledModuleSlot(router.getModules(), slot, MODULE_XPOS + slot * SLOT_X_SPACING, MODULE_YPOS));
        }
        // item router upgrades
        for (int slot = 0; slot < router.getUpgradeSlotCount(); slot++) {
            addSlot(new SlotItemHandler(router.getUpgrades(), slot, UPGRADE_XPOS + slot * SLOT_X_SPACING, UPGRADE_YPOS));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return router.getWorld().getTileEntity(router.getPos()) == router
                && router.getDistanceSq(player.getPosX(), player.getPosY(), player.getPosZ()) <= 64;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int sourceSlotIndex) {
        Slot sourceSlot = inventorySlots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (sourceSlotIndex < TE_FIRST_SLOT) {
            // This is a vanilla container slot so merge the stack into the appropriate part of the router's inventory
            if (sourceStack.getItem() instanceof ItemModule) {
                // shift-clicked a module: see if there's a free module slot
                if (!mergeItemStack(sourceStack, TE_FIRST_SLOT + MODULE_SLOT_START, TE_FIRST_SLOT + MODULE_SLOT_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (sourceStack.getItem() instanceof ItemUpgrade) {
                // shift-clicked an upgrade: see if there's a free upgrade slot
                if (!mergeItemStack(sourceStack, TE_FIRST_SLOT + UPGRADE_SLOT_START, TE_FIRST_SLOT + UPGRADE_SLOT_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // try to merge item into the router's buffer slot
                if (!mergeItemStack(sourceStack, TE_FIRST_SLOT + BUFFER_SLOT, TE_FIRST_SLOT + BUFFER_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (sourceSlotIndex < TE_FIRST_SLOT + TE_LAST_SLOT) {
            // This is a router slot, so merge the stack into the players inventory
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

    public TileEntityItemRouter getRouter() {
        return router;
    }

    public static class InstalledModuleSlot extends SlotItemHandler {
        // this is just so the slot can be easily identified for item tooltip purposes
        InstalledModuleSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }
    }
}
