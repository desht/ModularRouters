package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.api.matching.IModuleFlags;
import me.desht.modularrouters.logic.settings.ModuleFlags;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class BulkItemMatcher implements IItemMatcher {
    private final SetofItemStack stacks;
    private final Set<TagKey<Item>> tags;

    public BulkItemMatcher(SetofItemStack stacks, ModuleFlags flags) {
        this.stacks = stacks;
        this.tags = Sets.newHashSet();
        if (flags.matchItemTags()) {
            for (ItemStack stack : stacks) {
                tags.addAll(MiscUtil.itemTags(stack.getItem()));
            }
        }
    }

    @Override
    public boolean matchItem(ItemStack stack, IModuleFlags flags, HolderLookup.Provider registryAccess) {
        if (stacks.contains(stack)) {
            return true;
        } else {
            return flags.matchItemTags() && checkForItemTagIntersection(stack);
        }
    }

    private boolean checkForItemTagIntersection(ItemStack stack) {
        return !Sets.intersection(MiscUtil.itemTags(stack.getItem()), tags).isEmpty();
    }

}
