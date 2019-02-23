package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants;

public abstract class BaseModuleHandler extends GhostItemHandler {
    private final ItemStack holderStack;
    private final String tagName;

    public BaseModuleHandler(ItemStack holderStack, int size, String tagName) {
        super(size);
        this.holderStack = holderStack;
        this.tagName = tagName;

        deserializeNBT(holderStack.getOrCreateTag().getCompound(tagName));
    }

    /**
     * Get the itemstack which holds this filter.  Could be a module, could be a bulk item filter...
     *
     * @return the holding itemstack
     */
    public ItemStack getHolderStack() {
        return holderStack;
    }

    /**
     * Get the number of items in the filter of the given itemstack.  Counts the items without loading the NBT for
     * every item.
     *
     * @param tagName name of the NBT tag the data is under
     * @return number of items in the filter
     */
    public static int getFilterSize(ItemStack holderStack, String tagName) {
        return holderStack.getOrCreateTag().getList(tagName, Constants.NBT.TAG_COMPOUND).size();
    }

    /**
     * Save the contents of the item handler onto the holder item stack's NBT
     */
    public void save() {
        holderStack.getOrCreateTag().put(tagName, serializeNBT());
    }

    public static class BulkFilterHandler extends BaseModuleHandler {
        public BulkFilterHandler(ItemStack holderStack) {
            super(holderStack, BulkItemFilter.FILTER_SIZE, ModuleHelper.NBT_FILTER);
        }
    }

    public static class ModuleFilterHandler extends BaseModuleHandler {
        public ModuleFilterHandler(ItemStack holderStack) {
            super(holderStack, Filter.FILTER_SIZE, ModuleHelper.NBT_FILTER);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.getItem() instanceof ItemModule && ((ItemModule) stack.getItem()).isItemValidForFilter(stack)) {
                return super.insertItem(slot, stack, simulate);
            } else {
                return stack;
            }
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (stack.getItem() instanceof ItemModule && ((ItemModule) stack.getItem()).isItemValidForFilter(stack)) {
                super.setStackInSlot(slot, stack);
            }
        }
    }
}
