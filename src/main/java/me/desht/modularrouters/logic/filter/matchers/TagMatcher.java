package me.desht.modularrouters.logic.filter.matchers;

import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public class TagMatcher implements IItemMatcher {
    private final Collection<TagKey<Item>> tagList;

    public TagMatcher(Collection<TagKey<Item>> tagList) {
        this.tagList = tagList;
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        return tagList.stream().anyMatch(stack::is);
    }
}
