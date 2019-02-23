package me.desht.modularrouters.logic.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleFlags;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.filter.matchers.FluidMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
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

    public Filter(ItemStack moduleStack) {
        if (moduleStack.getItem() instanceof ItemModule && moduleStack.hasTag()) {
            flags = new Flags(moduleStack);
            NBTTagList tagList = moduleStack.getTag().getList(ModuleHelper.NBT_FILTER, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); ++i) {
                NBTTagCompound tagCompound = tagList.getCompound(i);
                ItemStack filterStack = ItemStack.read(tagCompound);
                IItemMatcher matcher = createMatcher(filterStack, moduleStack);
                if (matcher != null) {
                    matchers.add(matcher);
                }
            }
        } else {
            flags = Flags.DEFAULT_FLAGS;
        }
    }

    private IItemMatcher createMatcher(ItemStack filterStack, ItemStack moduleStack) {
        if (filterStack.getItem() instanceof ItemSmartFilter) {
            return ((ItemSmartFilter) filterStack.getItem()).compile(filterStack, moduleStack);
        } else {
            return moduleStack.getItem() instanceof ItemModule ?
                    ((ItemModule) moduleStack.getItem()).getFilterItemMatcher(moduleStack) : null;
        }
    }

    /**
     * Check if this filter would allow the given item stack through.  Will always return false if the item
     * stack is empty.
     * @param stack the stack to test
     * @return true if the stack should be passed, false otherwise
     */
    @Override
    public boolean test(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        for (IItemMatcher matcher : matchers) {
            if (matcher.matchItem(stack, flags)) {
                return !flags.isBlacklist();
            }
        }

        // no matches: test if this is a blacklist, fail if a whitelist
        return flags.isBlacklist();
    }

    public boolean testFluid(Fluid fluid) {
        for (IItemMatcher matcher : matchers) {
            if (matcher instanceof FluidMatcher && ((FluidMatcher) matcher).matchFluid(fluid)) {
                return !flags.isBlacklist();
            }
        }
        return flags.isBlacklist();
    }

    public Flags getFlags() {
        return flags;
    }

    public static class Flags {
        public static final Flags DEFAULT_FLAGS = new Flags();

        private final boolean blacklist;
        private final boolean ignoreMeta;
        private final boolean ignoreNBT;
        private final boolean ignoreTags;

        public Flags(ItemStack moduleStack) {
            Validate.isTrue(moduleStack.getItem() instanceof ItemModule);
            blacklist = ModuleHelper.isBlacklist(moduleStack);
            ignoreMeta = ModuleHelper.ignoreMeta(moduleStack);
            ignoreNBT = ModuleHelper.ignoreNBT(moduleStack);
            ignoreTags = ModuleHelper.ignoreTags(moduleStack);
        }

        public Flags() {
            blacklist = ModuleFlags.BLACKLIST.getDefaultValue();
            ignoreMeta = ModuleFlags.IGNORE_META.getDefaultValue();
            ignoreNBT = ModuleFlags.IGNORE_NBT.getDefaultValue();
            ignoreTags = ModuleFlags.IGNORE_OREDICT.getDefaultValue();
        }

        public Flags(byte mask) {
            blacklist = (mask & ModuleFlags.BLACKLIST.getMask()) != 0;
            ignoreMeta = (mask & ModuleFlags.IGNORE_META.getMask()) != 0;
            ignoreNBT = (mask & ModuleFlags.IGNORE_NBT.getMask()) != 0;
            ignoreTags = (mask & ModuleFlags.IGNORE_OREDICT.getMask()) != 0;
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

        public boolean isIgnoreTags() {
            return ignoreTags;
        }

        public static Flags with(ModuleFlags... flags) {
            byte mask = 0;
            for (ModuleFlags flag : flags) {
                mask |= flag.getMask();
            }
            return new Flags(mask);
        }
    }
}
