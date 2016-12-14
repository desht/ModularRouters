package me.desht.modularrouters.container;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

public class FilterHandler extends GhostItemHandler {
    private final ItemStack holderStack;

    public FilterHandler(ItemStack holderStack, int size) {
        super(size);
        this.holderStack = holderStack;

        if (!holderStack.hasTagCompound()) {
            holderStack.setTagCompound(new NBTTagCompound());
        }
        deserializeNBT(holderStack.getTagCompound().getTagList(ModuleHelper.NBT_FILTER, Constants.NBT.TAG_COMPOUND));
    }

    /**
     * Get the itemstack which holds this filter.  Could be a module, could be a bulk item filter...
     *
     * @return the holding itemstack
     */
    public ItemStack getHoldingItemStack() {
        return holderStack;
    }

    /**
     * Get the number of items in the filter of the given itemstack.  Counts the items without loading the NBT for
     * every item.
     *
     * @param holderStack item which holds the filter
     * @return number of items in the filter
     */
    public static int getItemCount(ItemStack holderStack) {
        if (holderStack.hasTagCompound()) {
            return holderStack.getTagCompound().getTagList(ModuleHelper.NBT_FILTER, Constants.NBT.TAG_COMPOUND).tagCount();
        } else {
            return 0;
        }
    }

    /**
     * Save the contents of the item handler onto the holder item stack's NBT
     */
    public void save() {
        holderStack.getTagCompound().setTag(ModuleHelper.NBT_FILTER, serializeNBT());
    }

    public static class BulkFilterHandler extends FilterHandler {
        public BulkFilterHandler(ItemStack holderStack) {
            super(holderStack, BulkItemFilter.FILTER_SIZE);
        }
    }

    public static class ModuleFilterHandler extends FilterHandler {
        public ModuleFilterHandler(ItemStack holderStack) {
            super(holderStack, Filter.FILTER_SIZE);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            Module module = ItemModule.getModule(getHoldingItemStack());
            return module.isItemValidForFilter(stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            Module module = ItemModule.getModule(getHoldingItemStack());
            if (module.isItemValidForFilter(stack)) {
                super.setStackInSlot(slot, stack);
            }
        }
    }
}
