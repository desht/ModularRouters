package me.desht.modularrouters.logic.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.logic.settings.ModuleFlags;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Filter implements Predicate<ItemStack> {
    public static final int FILTER_SIZE = 9;

    private final ModuleFlags flags;
    private final List<IItemMatcher> matchers = Lists.newArrayList();
    private final List<ItemStack> rawStacks = Lists.newArrayList();
    private final boolean roundRobin;
    private int rrCounter = 0;
    @Nullable
    private final HolderLookup.Provider registryAccess;

    public Filter() {
        flags = ModuleFlags.DEFAULT;
        roundRobin = false;
        registryAccess = null;
    }

    public Filter(ItemStack moduleStack, boolean storeRaw, boolean roundRobin, @Nullable HolderLookup.Provider registryAccess) {
        Validate.isTrue(moduleStack.getItem() instanceof ModuleItem, "item stack is not a module item! " + moduleStack.getItem());

        this.roundRobin = roundRobin;
        this.rrCounter = roundRobin ? ModuleItem.getRoundRobinCounter(moduleStack) : 0;
        this.registryAccess = registryAccess;

        flags = ModuleFlags.forItem(moduleStack);

        ModuleFilterHandler filterHandler = new ModuleFilterHandler(moduleStack, null);
        for (int i = 0; i < filterHandler.getSlots(); i++) {
            ItemStack filterStack = filterHandler.getStackInSlot(i);
            if (!filterStack.isEmpty()) {
                IItemMatcher matcher = createMatcher(filterStack, moduleStack);
                matchers.add(matcher);
                if (storeRaw) {
                    rawStacks.add(filterStack);
                }
            }
        }
        if (roundRobin && rrCounter >= matchers.size()) {
            rrCounter = 0;
        }
    }

    @Nonnull
    private IItemMatcher createMatcher(ItemStack filterStack, ItemStack moduleStack) {
        if (filterStack.getItem() instanceof SmartFilterItem sf) {
            return sf.compile(filterStack, moduleStack);
        } else {
            return ((ModuleItem) moduleStack.getItem()).getFilterItemMatcher(filterStack);
        }
    }

    public List<ItemStack> getRawStacks() {
        return rawStacks;
    }

    public boolean isEmpty() {
        return matchers.isEmpty();
    }

    public boolean isWhiteList() {
        return flags.whiteList();
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

        if (roundRobin && !matchers.isEmpty()) {
            // just match against a single item in the filter
            return matchers.get(rrCounter).matchItem(stack, flags, registryAccess) == flags.whiteList();
        } else {
            // match against everything in the filter (either match any or match all)
            boolean matchAll = flags.matchAllItems();

            for (IItemMatcher matcher : matchers) {
                boolean matchedOne = matcher.matchItem(stack, flags, registryAccess);
                if (!matchAll && matchedOne || matchAll && !matchedOne) {
                    return matchAll != flags.whiteList();
                }
            }

            // no matches: test succeeds if this is a blacklist, fails if a whitelist
            return matchAll == flags.whiteList();
        }
    }

    public Optional<Integer> cycleRoundRobin() {
        if (roundRobin && ++rrCounter >= matchers.size()) {
            rrCounter = 0;
        }
        return roundRobin ? Optional.of(rrCounter) : Optional.empty();
    }

    public boolean testFluid(Fluid fluid) {
        for (IItemMatcher matcher : matchers) {
            if (matcher.matchFluid(fluid, flags, registryAccess)) {
                return flags.whiteList();
            }
        }
        return !flags.whiteList();
    }

    public ModuleFlags getFlags() {
        return flags;
    }
}
