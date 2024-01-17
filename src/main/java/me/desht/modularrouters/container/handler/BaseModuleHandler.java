package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class BaseModuleHandler extends GhostItemHandler {
    private final ItemStack holderStack;
    private final ModularRouterBlockEntity router;
    private final String tagName;

    public BaseModuleHandler(ItemStack holderStack, ModularRouterBlockEntity router, int size, String tagName) {
        super(size);
        this.holderStack = holderStack;
        this.router = router;
        this.tagName = tagName;

        deserializeNBT(ModuleHelper.validateNBT(holderStack).getCompound(tagName));
    }

    /**
     * Get the itemstack which holds this filter.  Could be a module, could be a bulk item filter...
     *
     * @return the holding itemstack
     */
    public ItemStack getHolderStack() {
        return holderStack;
    }

    @Override
    protected void onContentsChanged(int slot) {
        save();

        if (router != null) {
            router.recompileNeeded(ModularRouterBlockEntity.COMPILE_MODULES);
        }
    }

    /**
     * Get the number of items in the filter of the given itemstack.  Counts the items without loading the NBT for
     * every item.
     *
     * @param tagName name of the NBT tag the data is under
     * @return number of items in the filter
     */
    public static int getFilterSize(ItemStack holderStack, String tagName) {
        CompoundTag tag = holderStack.getTagElement(ModularRouters.MODID);
        if (tag != null  && tag.contains(tagName)) {
            ModuleFilterHandler handler = new ModuleFilterHandler(holderStack, null);
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
    }

    /**
     * Save the contents of the item handler onto the holder item stack's NBT
     */
    public void save() {
        ModuleHelper.validateNBTForWriting(holderStack).put(tagName, serializeNBT());
    }

    public static class BulkFilterHandler extends BaseModuleHandler {
        public BulkFilterHandler(ItemStack holderStack, ModularRouterBlockEntity router) {
            super(holderStack, router, BulkItemFilter.FILTER_SIZE, ModuleHelper.NBT_FILTER);
        }
    }

    public static class ModuleFilterHandler extends BaseModuleHandler {
        public ModuleFilterHandler(ItemStack holderStack, ModularRouterBlockEntity router) {
            super(holderStack, router, Filter.FILTER_SIZE, ModuleHelper.NBT_FILTER);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return ((ModuleItem) getHolderStack().getItem()).isItemValidForFilter(stack);
        }
    }
}
