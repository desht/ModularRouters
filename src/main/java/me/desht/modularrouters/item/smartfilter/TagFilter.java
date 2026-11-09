package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.container.TagFilterMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.TagMatcher;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TagFilter extends SmartFilterItem {
    public static List<TagKey<Item>> getTagList(ItemStack filterStack) {
        List<String> strings = filterStack.getOrDefault(ModDataComponents.FILTER_STRINGS, List.of());
        return strings.stream()
                .filter(rl -> ResourceLocation.tryParse(rl) != null)
                .map(s -> TagKey.create(Registries.ITEM, ResourceLocation.parse(s)))
                .toList();
    }

    @NotNull
    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        return new TagMatcher(getTagList(filterStack));
    }

    @Override
    public void addExtraInformation(ItemStack stack, List<Component> list) {
        super.addExtraInformation(stack, list);

        List<TagKey<Item>> l = getTagList(stack);
        addCountInfo(list, l.size());
        list.addAll(l.stream()
                .map(s -> " • " + ChatFormatting.AQUA + s.location())
                .map(Component::literal)
                .toList());
    }

    @Override
    public int getSize(ItemStack filterStack) {
        return getTagList(filterStack).size();
    }

    @Override
    public AbstractSmartFilterMenu createMenu(int windowId, Inventory invPlayer, MFLocator loc) {
        return new TagFilterMenu(windowId, invPlayer, loc);
    }
}
