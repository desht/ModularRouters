package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.logic.filter.Filter.Flags;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Set;

public class BulkItemMatcher implements IItemMatcher {
    private final SetofItemStack stacks;
    private final Set<Integer> oreDictIds;

    public BulkItemMatcher(SetofItemStack stacks, Flags flags) {
        this.stacks = stacks;
        this.oreDictIds = Sets.newHashSet();
        if (!flags.isIgnoreOredict()) {
            for (ItemStack stack : stacks) {
                for (int id : OreDictionary.getOreIDs(stack)) {
                    oreDictIds.add(id);
                }
            }
        }
    }

    @Override
    public boolean matchItem(ItemStack stack, Flags flags) {
        if (stacks.contains(stack)) {
            return true;
        } else if (!flags.isIgnoreOredict() && matchOreDict(stack)) {
            return true;
        }
        return false;
    }

    private boolean matchOreDict(ItemStack stack) {
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (oreDictIds.contains(id)) {
                return true;
            }
        }
        return false;
    }
}
