package me.desht.modularrouters.container;

import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.Constants;

public class FilterHandler extends GhostItemHandler {
    private final ItemStack moduleStack;

    public FilterHandler(ItemStack moduleStack) {
        this(moduleStack, Filter.FILTER_SIZE);
    }

    public FilterHandler(ItemStack moduleStack, int size) {
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
}
