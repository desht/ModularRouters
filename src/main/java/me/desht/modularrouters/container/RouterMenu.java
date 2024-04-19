package me.desht.modularrouters.container;

import me.desht.modularrouters.api.event.RegisterRouterContainerData;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.Comparator;
import java.util.Map;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;
import static me.desht.modularrouters.container.Layout.SLOT_Y_SPACING;

public class RouterMenu extends AbstractMRContainerMenu {
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

    private final ModularRouterBlockEntity router;
    public final ModularRouterBlockEntity.TrackedEnergy data;

    public RouterMenu(int windowId, Inventory invPlayer, FriendlyByteBuf extraData) {
        this(windowId, invPlayer, extraData.readBlockPos());
    }

    public RouterMenu(int windowId, Inventory invPlayer, BlockPos routerPos) {
        super(ModMenuTypes.ROUTER_MENU.get(), windowId);

        this.router = invPlayer.player.level().getBlockEntity(routerPos, ModBlockEntities.MODULAR_ROUTER.get())
                .orElseThrow(() -> new IllegalStateException("router missing at " + routerPos));

        data = router.trackedEnergy;

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

        addDataSlots(data);

        final var event = new RegisterRouterContainerData(router);
        NeoForge.EVENT_BUS.post(event);
        event.getData().entrySet()
                .stream().sorted(Map.Entry.comparingByKey())
                .forEach(entry -> addDataSlot(entry.getValue()));
    }

    @Override
    public boolean stillValid(Player player) {
        return !router.isRemoved() && Vec3.atCenterOf(router.getBlockPos()).distanceToSqr(player.position()) <= 64;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int sourceSlotIndex) {
        Slot sourceSlot = slots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (sourceSlotIndex < TE_FIRST_SLOT) {
            // This is a vanilla container slot so merge the stack into the appropriate part of the router's inventory
            if (sourceStack.getItem() instanceof ModuleItem) {
                // shift-clicked a module: see if there's a free module slot
                if (!moveItemStackTo(sourceStack, TE_FIRST_SLOT + MODULE_SLOT_START, TE_FIRST_SLOT + MODULE_SLOT_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (sourceStack.getItem() instanceof UpgradeItem) {
                // shift-clicked an upgrade: see if there's a free upgrade slot
                if (!moveItemStackTo(sourceStack, TE_FIRST_SLOT + UPGRADE_SLOT_START, TE_FIRST_SLOT + UPGRADE_SLOT_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // try to merge item into the router's buffer slot
                if (!moveItemStackTo(sourceStack, TE_FIRST_SLOT + BUFFER_SLOT, TE_FIRST_SLOT + BUFFER_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (sourceSlotIndex < TE_FIRST_SLOT + TE_LAST_SLOT) {
            // This is a router slot, so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, 0, TE_FIRST_SLOT - 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.err.print("Invalid moduleSlotIndex: " + sourceSlotIndex);
            return ItemStack.EMPTY;
        }

        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }

    public ModularRouterBlockEntity getRouter() {
        return router;
    }

    public static class InstalledModuleSlot extends SlotItemHandler {
        // this is just so the slot can be easily identified for item tooltip purposes
        InstalledModuleSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }
    }
}
