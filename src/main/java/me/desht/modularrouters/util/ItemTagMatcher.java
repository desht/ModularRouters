package me.desht.modularrouters.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class ItemTagMatcher {
    private final Set<TagKey<Item>> tags;

    public ItemTagMatcher(ItemStack stack) {
        this.tags = ImmutableSet.copyOf(MiscUtil.itemTags(stack.getItem()));
    }

    public boolean match(ItemStack stack) {
        return !Sets.intersection(tags, MiscUtil.itemTags(stack.getItem())).isEmpty();
    }
}
