package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.logic.filter.Filter.Flags;
import me.desht.modularrouters.util.HashableItemStackWrapper;
import me.desht.modularrouters.util.TagOwnerTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class BulkItemMatcher implements IItemMatcher {
    private final Set<HashableItemStackWrapper> stacks;
    private final Set<ResourceLocation> tags;

    public BulkItemMatcher(Set<HashableItemStackWrapper> stacks, Flags flags) {
        this.stacks = stacks;
        this.tags = Sets.newHashSet();
        if (!flags.isIgnoreTags()) {
            for (HashableItemStackWrapper hStack : stacks) {
                tags.addAll(TagOwnerTracker.getInstance().getOwningTags(ItemTags.getCollection(), hStack.getStack().getItem()));
            }
        }
    }

    @Override
    public boolean matchItem(ItemStack stack, Flags flags) {
        if (stacks.contains(new HashableItemStackWrapper(stack, flags))) {
            return true;
        } else {
            return !flags.isIgnoreTags() && matchTags(stack);
        }
    }

    private boolean matchTags(ItemStack stack) {
        Set<ResourceLocation> s = new HashSet<>(tags);
        s.retainAll(TagOwnerTracker.getInstance().getOwningTags(ItemTags.getCollection(), stack.getItem()));
        return !s.isEmpty();
    }
}
