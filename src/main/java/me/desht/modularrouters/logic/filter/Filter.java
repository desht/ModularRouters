package me.desht.modularrouters.logic.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.SmartFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.SimpleItemMatcher;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.Validate;

import java.util.List;

public class Filter {
    public static final int FILTER_SIZE = 9;
    private final Flags flags;
    private final List<IItemMatcher> matchers = Lists.newArrayList();

    public Filter() {
        flags = new Flags();
    }

    public Filter(ModuleTarget target, ItemStack moduleStack) {
        if (moduleStack.getItem() instanceof ItemModule && moduleStack.hasTagCompound()) {
            flags = new Flags(moduleStack);
            NBTTagList tagList = moduleStack.getTagCompound().getTagList(ModuleHelper.NBT_FILTER, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.tagCount(); ++i) {
                NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
                ItemStack filterStack = ItemStack.loadItemStackFromNBT(tagCompound);
                matchers.add(createMatcher(filterStack, moduleStack, target));
            }
        } else {
            flags = new Flags();
        }
    }

    private IItemMatcher createMatcher(ItemStack filterStack, ItemStack moduleStack, ModuleTarget target) {
        if (filterStack.getItem() instanceof ItemSmartFilter) {
            SmartFilter f = ItemSmartFilter.getFilter(filterStack);
            return f.compile(filterStack, moduleStack, target);
        } else {
            return new SimpleItemMatcher(filterStack);
        }
    }

    public boolean pass(ItemStack stack) {
        for (IItemMatcher matcher : matchers) {
            if (matcher.matchItem(stack, flags)) {
                return !flags.isBlacklist();
            }
        }

        // no matches: pass if this is a blacklist, fail if a whitelist
        return flags.isBlacklist();
    }

    public Flags getFlags() {
        return flags;
    }

    public class Flags {
        private final boolean blacklist;
        private final boolean ignoreMeta;
        private final boolean ignoreNBT;
        private final boolean ignoreOredict;

        public Flags(ItemStack moduleStack) {
            Validate.isTrue(moduleStack.getItem() instanceof ItemModule);

            Module module = ItemModule.getModule(moduleStack);
            blacklist = ModuleHelper.isBlacklist(moduleStack);
            ignoreMeta = ModuleHelper.ignoreMeta(moduleStack);
            ignoreNBT = ModuleHelper.ignoreNBT(moduleStack);
            ignoreOredict = ModuleHelper.ignoreOreDict(moduleStack);
        }

        public Flags() {
            blacklist = false;
            ignoreMeta = false;
            ignoreNBT = true;
            ignoreOredict = true;
        }

        public boolean isBlacklist() {
            return blacklist;
        }

        public boolean isIgnoreMeta() {
            return ignoreMeta;
        }

        public boolean isIgnoreNBT() {
            return ignoreNBT;
        }

        public boolean isIgnoreOredict() {
            return ignoreOredict;
        }

    }
}
