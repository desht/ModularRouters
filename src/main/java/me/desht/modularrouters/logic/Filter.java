package me.desht.modularrouters.logic;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    private boolean blacklist = false;
    private boolean ignoreMeta = false;
    private boolean ignoreNBT = false;
    private boolean ignoreOredict = false;
    private final List<ItemStack> items = new ArrayList<>();
    private final List<int[]> oreIdCache = new ArrayList<>();

    public Filter() {
        initDefault();
    }

    public Filter(ItemStack moduleStack) {
        if (moduleStack.getItem() instanceof ItemModule) {
            Module module = ItemModule.getModule(moduleStack);
            blacklist = module.isBlacklist(moduleStack);
            ignoreMeta = module.ignoreMeta(moduleStack);
            ignoreNBT = module.ignoreNBT(moduleStack);
            ignoreOredict = module.ignoreOreDict(moduleStack);
            // it's safe to assume that the module actually has NBT data at this point...
            NBTTagList tagList = moduleStack.getTagCompound().getTagList("ModuleFilter", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.tagCount(); ++i) {
                NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
                ItemStack filterStack = ItemStack.loadItemStackFromNBT(tagCompound);
                items.add(filterStack);
                oreIdCache.add(OreDictionary.getOreIDs(filterStack));
            }
        } else {
            initDefault();
        }
    }

    private void initDefault() {
        blacklist = false;
        ignoreMeta = false;
        ignoreNBT = true;
        ignoreOredict = true;
    }

    public void clear() {
        items.clear();
    }

    public boolean pass(ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack filterItem = items.get(i);
            boolean matched = compareOne(i, stack);
            if (matched) {
                return !blacklist;
            }
        }
        // no matches: pass if this is a blacklist, fail if a whitelist
        return blacklist;
    }

    private boolean compareOne(int i, ItemStack stack) {
        ItemStack filterStack = items.get(i);
        if (Item.getIdFromItem(filterStack.getItem()) != Item.getIdFromItem(stack.getItem())) {
            return !ignoreOredict && isOreDictMatch(i, stack);
        }
        if (!ignoreMeta && filterStack.getItemDamage() != stack.getItemDamage()) {
            return false;
        }
        if (!ignoreNBT) {
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

    private boolean isOreDictMatch(int index, ItemStack stack) {
        int[] ids1 = OreDictionary.getOreIDs(stack);
        int[] ids2 = oreIdCache.get(index);
        if (ids1 == null || ids1.length == 0 || ids2 == null || ids2.length == 0) {
            return false;
        }

        // TODO: test if a Set<Integer> might be more efficient?

        for (int i1 : ids1) {
            for (int i2 : ids2) {
                if (i1 == i2) {
                    return true;
                }
            }
        }

        return false;
    }
}
