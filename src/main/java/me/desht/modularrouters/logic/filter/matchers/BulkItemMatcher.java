package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.logic.filter.Filter.Flags;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Set;

public class BulkItemMatcher implements IItemMatcher {
    private final SetofItemStack stacks;
    private final Set<ResourceLocation> tags;

    public BulkItemMatcher(SetofItemStack stacks, Flags flags) {
        this.stacks = stacks;
        this.tags = Sets.newHashSet();
        if (!flags.isIgnoreTags()) {
            for (ItemStack stack : stacks) {
                tags.addAll(stack.getItem().getTags());
            }
        }
    }

    @Override
    public boolean matchItem(ItemStack stack, Flags flags) {
        if (stacks.contains(stack)) {
            return true;
        } else {
            return !flags.isIgnoreTags() && matchTags(stack);
        }
    }

    private boolean matchTags(ItemStack stack) {
        return !Sets.intersection(stack.getItem().getTags(), tags).isEmpty();
    }
}
