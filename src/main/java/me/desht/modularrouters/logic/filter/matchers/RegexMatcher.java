package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

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
                ModularRouters.logger.warn("can't compile '" + r + "' - " + e.getMessage());
            }
        }
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        if (stack == null) return false;
        String name = TextFormatting.getTextWithoutFormattingCodes(stack.getDisplayName());
        if (name == null) name = stack.getUnlocalizedName();
        for (Pattern pat : patterns) {
            if (pat.matcher(name).find()) {
                return true;
            }
        }
        return false;
    }
}
