package me.desht.modularrouters.logic.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.Module.ModuleFlags;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.SmartFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.function.Predicate;

public class Filter implements Predicate<ItemStack> {
    public static final int FILTER_SIZE = 9;

    private final Flags flags;
    private final List<IItemMatcher> matchers = Lists.newArrayList();

    public Filter() {
        flags = Flags.DEFAULT_FLAGS;
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
            flags = Flags.DEFAULT_FLAGS;
        }
    }

    private IItemMatcher createMatcher(ItemStack filterStack, ItemStack moduleStack, ModuleTarget target) {
        if (filterStack.getItem() instanceof ItemSmartFilter) {
            SmartFilter f = ItemSmartFilter.getFilter(filterStack);
            return f.compile(filterStack, moduleStack, target);
        } else {
            Module module = ItemModule.getModule(moduleStack);
            return module.getFilterItemMatcher(filterStack);
        }
    }

    @Override
    public boolean test(ItemStack stack) {
        for (IItemMatcher matcher : matchers) {
            if (matcher.matchItem(stack, flags)) {
                return !flags.isBlacklist();
            }
        }

        // no matches: test if this is a blacklist, fail if a whitelist
        return flags.isBlacklist();
    }

    public Flags getFlags() {
        return flags;
    }

    public static class Flags {
        static final Flags DEFAULT_FLAGS = new Flags();

        private final boolean blacklist;
        private final boolean ignoreMeta;
        private final boolean ignoreNBT;
        private final boolean ignoreOredict;

        public Flags(ItemStack moduleStack) {
            Validate.isTrue(moduleStack.getItem() instanceof ItemModule);
            blacklist = ModuleHelper.isBlacklist(moduleStack);
            ignoreMeta = ModuleHelper.ignoreMeta(moduleStack);
            ignoreNBT = ModuleHelper.ignoreNBT(moduleStack);
            ignoreOredict = ModuleHelper.ignoreOreDict(moduleStack);
        }

        public Flags() {
            blacklist = ModuleFlags.BLACKLIST.getDefaultValue();
            ignoreMeta = ModuleFlags.IGNORE_META.getDefaultValue();
            ignoreNBT = ModuleFlags.IGNORE_NBT.getDefaultValue();
            ignoreOredict = ModuleFlags.IGNORE_OREDICT.getDefaultValue();
        }

        public Flags(byte mask) {
            blacklist = (mask & ModuleFlags.BLACKLIST.getMask()) != 0;
            ignoreMeta = (mask & ModuleFlags.IGNORE_META.getMask()) != 0;
            ignoreNBT = (mask & ModuleFlags.IGNORE_NBT.getMask()) != 0;
            ignoreOredict = (mask & ModuleFlags.IGNORE_OREDICT.getMask()) != 0;
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

        public static Flags with(Module.ModuleFlags... flags) {
            byte mask = 0;
            for (ModuleFlags flag : flags) {
                mask |= flag.getMask();
            }
            return new Flags(mask);
        }
    }
}
