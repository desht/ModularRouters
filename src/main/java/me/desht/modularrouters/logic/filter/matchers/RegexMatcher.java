package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexMatcher implements IItemMatcher {
    private final List<Pattern> patterns;

    public RegexMatcher(List<String> regex) {
        patterns = Lists.newArrayListWithCapacity(regex.size());

        for (String r : regex) {
            try {
                // TODO case sensitivity configurable?
                Pattern pat = Pattern.compile(r, Pattern.CASE_INSENSITIVE);
                patterns.add(pat);
            } catch (PatternSyntaxException e) {
                ModularRouters.LOGGER.warn("can't compile '" + r + "' - " + e.getMessage());
            }
        }
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        if (stack.isEmpty()) return false;
        String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return patterns.stream().anyMatch(pat -> pat.matcher(name).find());
    }
}
