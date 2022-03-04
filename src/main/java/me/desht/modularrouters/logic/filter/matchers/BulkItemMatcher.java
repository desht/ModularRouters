package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.logic.filter.Filter.Flags;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class BulkItemMatcher implements IItemMatcher {
    private final SetofItemStack stacks;
    private final Set<TagKey<Item>> tags;

    public BulkItemMatcher(SetofItemStack stacks, Flags flags) {
        this.stacks = stacks;
        this.tags = Sets.newHashSet();
        if (flags.matchTags()) {
            for (ItemStack stack : stacks) {
                tags.addAll(MiscUtil.itemTags(stack.getItem()));
            }
        }
    }

    @Override
    public boolean matchItem(ItemStack stack, Flags flags) {
        if (stacks.contains(stack)) {
            return true;
        } else {
            return flags.matchTags() && matchTags(stack);
        }
    }

    private boolean matchTags(ItemStack stack) {
        return !Sets.intersection(MiscUtil.itemTags(stack.getItem()), tags).isEmpty();
    }

}
