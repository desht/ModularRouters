package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

public class ModMatcher implements IItemMatcher {
    private final Set<String> modSet = Sets.newHashSet();

    public ModMatcher(List<String> mods) {
        modSet.addAll(mods);
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        return !stack.isEmpty() && modSet.contains(stack.getItem().getRegistryName().getNamespace());
    }
}
