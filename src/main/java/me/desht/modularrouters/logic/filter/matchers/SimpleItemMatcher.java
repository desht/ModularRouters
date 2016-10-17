package me.desht.modularrouters.logic.filter.matchers;

import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.logic.filter.OreDictMatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SimpleItemMatcher implements IItemMatcher {
    private final ItemStack filterStack;
    private final OreDictMatcher oreDictMatcher;

    public SimpleItemMatcher(ItemStack stack) {
        this.filterStack = stack;
        oreDictMatcher = new OreDictMatcher(stack);
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        if (Item.getIdFromItem(filterStack.getItem()) != Item.getIdFromItem(stack.getItem())) {
            return !flags.isIgnoreOredict() && oreDictMatcher.match(stack);
        }
        if (!flags.isIgnoreMeta() && filterStack.getItemDamage() != stack.getItemDamage()) {
            return !flags.isIgnoreOredict() && oreDictMatcher.match(stack);
        }
        if (!flags.isIgnoreNBT()) {
            NBTTagCompound filterTag = filterStack.getTagCompound();
            NBTTagCompound stackTag = stack.getTagCompound();
            if (filterTag == null && stackTag != null || filterTag != null && stackTag == null) {
                return false;
            } else if (filterTag == null) {
                return true;
            } else if (!filterTag.hasKey("GEN")) {
                return filterTag.equals(stackTag);
            } else {
                NBTTagCompound filterTag2 = filterTag.copy();
                NBTTagCompound itemTag2 = stackTag.copy();
                filterTag2.removeTag("GEN");
                itemTag2.removeTag("GEN");
                return filterTag2.equals(itemTag2);
            }
        }
        return true;
    }
}
