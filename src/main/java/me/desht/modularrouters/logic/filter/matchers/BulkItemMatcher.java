package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Set;

public class BulkItemMatcher implements IItemMatcher {
    private final Set<Integer> byId = Sets.newHashSet();
    private final Set<Long> byIdAndMeta = Sets.newHashSet();

    public BulkItemMatcher(SetofItemStack stacks, ModuleTarget target) {
        for (ItemStack stack : stacks) {
            int id = Item.getIdFromItem(stack.getItem());
            byId.add(id);
            byIdAndMeta.add(idAndMeta(stack));
        }
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        int id = Item.getIdFromItem(stack.getItem());
        if (flags.isIgnoreMeta()) {
            return byId.contains(id);
        } else {
            return byIdAndMeta.contains(idAndMeta(stack));
        }
    }

    private static long idAndMeta(ItemStack stack) {
        int id = Item.getIdFromItem(stack.getItem());
        return ((long)id << 32) | stack.getItemDamage();
    }
}
