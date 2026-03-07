package me.desht.modularrouters.container.handler;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A handler for "ghost" items - items that can be set but not actually inserted or extracted normally.
 * Used for filter slots where we want to store the item type but not the actual items.
 */
public class GhostItemHandler extends ItemStacksResourceHandler {
    public GhostItemHandler(int size) {
        super(size);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        // Ghost handlers don't accept normal insertion - use set() directly
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        // Ghost handlers don't allow normal extraction - use set() directly
        return 0;
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return 1;
    }

    /**
     * Directly overwrites the contents of the handler at a specific index.
     */
    public void setStackInSlot(int slot, ItemStack stack) {
        ItemResource resource = ItemResource.of(stack);
        set(slot, resource, Math.min(stack.getCount(), getCapacity(1, resource)));
    }

    /**
     * Get an ItemStack copy of the contents at the given slot.
     */
    public ItemStack getStackInSlot(int slot) {
        return ItemUtil.getStack(this, slot);
    }

    /**
     * Get the number of slots in this handler.
     * Provided for compatibility with code expecting the old IItemHandler interface.
     */
    public int getSlots() {
        return size();
    }
}
