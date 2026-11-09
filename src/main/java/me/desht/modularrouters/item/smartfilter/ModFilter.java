package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.container.ModFilterMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.ModMatcher;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModFilter extends SmartFilterItem {
    public static final int MAX_SIZE = 6;

    public ModFilter() {
        super(ModItems.defaultProps().component(ModDataComponents.FILTER_STRINGS, List.of()));
    }

    public static List<String> getModList(ItemStack filterStack) {
        return filterStack.getOrDefault(ModDataComponents.FILTER_STRINGS, List.of());
    }

    @Override
    public @NotNull IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        return new ModMatcher(getModList(filterStack));
    }

    @Override
    public void addExtraInformation(ItemStack stack, List<Component> list) {
        super.addExtraInformation(stack, list);

        List<String> mods = getModList(stack);
        addCountInfo(list, mods.size());
        list.addAll(mods.stream()
                .map(ModNameCache::getModName)
                .map(s -> " • " + ChatFormatting.AQUA + s)
                .map(Component::literal)
                .toList());
    }

    @Override
    public AbstractSmartFilterMenu createMenu(int windowId, Inventory invPlayer, MFLocator loc) {
        return new ModFilterMenu(windowId, invPlayer, loc);
    }

    @Override
    public int getSize(ItemStack filterStack) {
        return getModList(filterStack).size();
    }
}
