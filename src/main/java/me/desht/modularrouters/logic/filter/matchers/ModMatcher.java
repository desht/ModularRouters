package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.api.matching.IModuleFlags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

public class ModMatcher implements IItemMatcher {
    private final Set<String> modSet = Sets.newHashSet();

    public ModMatcher(List<String> mods) {
        modSet.addAll(mods);
    }

    @Override
    public boolean matchItem(ItemStack stack, IModuleFlags flags, HolderLookup.Provider registryAccess) {
        return !stack.isEmpty() && modSet.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
    }
}
