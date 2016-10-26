package me.desht.modularrouters.container;

import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

public class FilterHandler extends GhostItemHandler {
    private final ItemStack moduleStack;

    public FilterHandler(ItemStack moduleStack) {
        this(moduleStack, Filter.FILTER_SIZE);
    }

    public FilterHandler(ItemStack moduleStack, int size) {
        super(size);
        this.moduleStack = moduleStack;

        if (!moduleStack.hasTagCompound()) {
            moduleStack.setTagCompound(new NBTTagCompound());
        }
        deserializeNBT(moduleStack.getTagCompound().getTagList(Filter.NBT_FILTER, Constants.NBT.TAG_COMPOUND));
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
        moduleStack.getTagCompound().setTag(Filter.NBT_FILTER, serializeNBT());
    }
}
