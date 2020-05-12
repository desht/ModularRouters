package me.desht.modularrouters.logic.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleFlags;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.filter.matchers.FluidMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.function.Predicate;

public class Filter implements Predicate<ItemStack> {
    public static final int FILTER_SIZE = 9;

    private final Flags flags;
    private final List<IItemMatcher> matchers = Lists.newArrayList();
    private final boolean matchAll;

    public Filter() {
        flags = Flags.DEFAULT_FLAGS;
        matchAll = false;
    }

    public Filter(ItemStack moduleStack) {
        if (moduleStack.getItem() instanceof ItemModule && moduleStack.hasTag()) {
            flags = new Flags(moduleStack);
            matchAll = ModuleHelper.isMatchAll(moduleStack);
            ModuleFilterHandler filterHandler = new ModuleFilterHandler(moduleStack, null);
            for (int i = 0; i < filterHandler.getSlots(); i++) {
                ItemStack filterStack = filterHandler.getStackInSlot(i);
                if (!filterStack.isEmpty()) {
                    IItemMatcher matcher = createMatcher(filterStack, moduleStack);
                    if (matcher != null) {
                        matchers.add(matcher);
                    }
                }
            }
        } else {
            flags = Flags.DEFAULT_FLAGS;
            matchAll = false;
        }
    }

    private IItemMatcher createMatcher(ItemStack filterStack, ItemStack moduleStack) {
        if (filterStack.getItem() instanceof ItemSmartFilter) {
            return ((ItemSmartFilter) filterStack.getItem()).compile(filterStack, moduleStack);
        } else {
            return moduleStack.getItem() instanceof ItemModule ?
                    ((ItemModule) moduleStack.getItem()).getFilterItemMatcher(filterStack) : null;
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
            boolean matchedOne = matcher.matchItem(stack, flags);
            if (!matchAll && matchedOne || matchAll && !matchedOne) {
                return matchAll == flags.isBlacklist();
            }
        }

        // no matches: test succeeds if this is a blacklist, fails if a whitelist
        return matchAll != flags.isBlacklist();

    }

    public boolean testFluid(Fluid fluid) {
        for (IItemMatcher matcher : matchers) {
            if (matcher instanceof FluidMatcher && ((FluidMatcher) matcher).matchFluid(fluid, flags)) {
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
        private final boolean ignoreDamage;
        private final boolean ignoreNBT;
        private final boolean ignoreTags;

        public Flags(ItemStack moduleStack) {
            Validate.isTrue(moduleStack.getItem() instanceof ItemModule);
            blacklist = ModuleHelper.isBlacklist(moduleStack);
            ignoreDamage = ModuleHelper.ignoreDamage(moduleStack);
            ignoreNBT = ModuleHelper.ignoreNBT(moduleStack);
            ignoreTags = ModuleHelper.ignoreTags(moduleStack);
        }

        public Flags() {
            blacklist = ModuleFlags.BLACKLIST.getDefaultValue();
            ignoreDamage = ModuleFlags.IGNORE_DAMAGE.getDefaultValue();
            ignoreNBT = ModuleFlags.IGNORE_NBT.getDefaultValue();
            ignoreTags = ModuleFlags.IGNORE_TAGS.getDefaultValue();
        }

        public Flags(byte mask) {
            blacklist = (mask & ModuleFlags.BLACKLIST.getMask()) != 0;
            ignoreDamage = (mask & ModuleFlags.IGNORE_DAMAGE.getMask()) != 0;
            ignoreNBT = (mask & ModuleFlags.IGNORE_NBT.getMask()) != 0;
            ignoreTags = (mask & ModuleFlags.IGNORE_TAGS.getMask()) != 0;
        }

        public boolean isBlacklist() {
            return blacklist;
        }

        public boolean isIgnoreDamage() {
            return ignoreDamage;
        }

        public boolean isIgnoreNBT() {
            return ignoreNBT;
        }

        public boolean matchTags() {
            return !ignoreTags;
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
