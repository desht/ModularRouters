package me.desht.modularrouters.container;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants;

public class FilterHandler extends GhostItemHandler {
    private final ItemStack moduleStack;

    public FilterHandler(ItemStack moduleStack) {
        this(moduleStack, Filter.FILTER_SIZE);
    }

    FilterHandler(ItemStack moduleStack, int size) {
        super(size);
        this.moduleStack = moduleStack;

        ModuleHelper.validateNBT(moduleStack);
        deserializeNBT(moduleStack.getTagCompound().getTagList(ModuleHelper.NBT_FILTER, Constants.NBT.TAG_COMPOUND));
    }

    public ItemStack getModuleItemStack() {
        return moduleStack;
    }

    public void save() {
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].stackSize <= 0) {
                items[i] = null;
            }
        }
        moduleStack.getTagCompound().setTag(ModuleHelper.NBT_FILTER, serializeNBT());
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        Module module = ItemModule.getModule(moduleStack);
        return module.isItemValidForFilter(stack) ? super.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        Module module = ItemModule.getModule(moduleStack);
        if (module.isItemValidForFilter(stack)) {
            super.setStackInSlot(slot, stack);
        }
    }
}
