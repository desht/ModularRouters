package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.logic.filter.matchers.RegexMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class RegexFilter extends SmartFilterItem {
    public static final int MAX_SIZE = 6;

    public RegexFilter(Properties properties) {
        super(properties.component(ModDataComponents.FILTER_STRINGS, List.of()));
    }

    @Override
    public boolean hasMenu() {
        return false;
    }

    public static List<String> getRegexList(ItemStack filterStack) {
        return filterStack.getOrDefault(ModDataComponents.FILTER_STRINGS, List.of());
    }

    public static void setRegexList(ItemStack filterStack, List<String> regexList) {
        filterStack.set(ModDataComponents.FILTER_STRINGS, regexList);
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, Consumer<Component> tooltipAdder) {
        super.addExtraInformation(itemstack, tooltipAdder);

        List<String> regexList = getRegexList(itemstack);
        addCountInfo(tooltipAdder, regexList.size());
        regexList.stream().map(s -> " • " + ChatFormatting.AQUA + "/" + s + "/").map(Component::literal).forEach(tooltipAdder);
    }

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        return new RegexMatcher(getRegexList(filterStack));
    }

    @Override
    public int getSize(ItemStack filterStack) {
        return getRegexList(filterStack).size();
    }
}
