package me.desht.modularrouters.logic.filter.matchers;

import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.api.matching.IModuleFlags;
import net.minecraft.core.HolderLookup;
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
    public boolean matchItem(ItemStack stack, IModuleFlags flags, HolderLookup.Provider registryAccess) {
        return tagList.stream().anyMatch(stack::is);
    }
}
