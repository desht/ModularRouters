package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

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
        if (holderStack.hasTag() && holderStack.getTag().contains(ModuleHelper.NBT_FILTER)) {
            ModuleFilterHandler handler = new ModuleFilterHandler(holderStack);
            int n = 0;
            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) {
                    n++;
                }
            }
            return n;
        } else {
            return 0;
        }
//        return holderStack.getOrCreateTag().getList(tagName, Constants.NBT.TAG_COMPOUND).size();
    }

    @Override
    protected void onContentsChanged(int slot) {
        save();
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
            return ((ItemModule) getHolderStack().getItem()).isItemValidForFilter(stack) ?
                    super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
//            if (((ItemModule) getHolderStack().getItem()).isItemValidForFilter(stack)) {
                super.setStackInSlot(slot, stack);
//            }
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return ((ItemModule) getHolderStack().getItem()).isItemValidForFilter(stack);
        }
    }
}
