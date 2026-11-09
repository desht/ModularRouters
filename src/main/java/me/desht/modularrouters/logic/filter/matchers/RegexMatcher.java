package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.api.matching.IModuleFlags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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
                ModularRouters.LOGGER.warn("can't compile '{}' - {}", r, e.getMessage());
            }
        }
    }

    @Override
    public boolean matchItem(ItemStack stack, IModuleFlags flags, HolderLookup.Provider registryAccess) {
        if (stack.isEmpty()) return false;
        String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        try {
            return patterns.stream().anyMatch(pat -> pat.matcher(new LimitedReadCharSequence(name)).find());
        } catch (IllegalStateException e) {
            // probably evil regex?
            return false;
        }
    }

    // a little bit kludgy but provides some protection against ReDOS attacks
    private static class LimitedReadCharSequence implements CharSequence {
        private static final int MAX_READS = 256;

        private final String wrapped;
        private int charsRead = 0;

        private LimitedReadCharSequence(String wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int length() {
            return wrapped.length();
        }

        @Override
        public char charAt(int index) {
            if (++charsRead > MAX_READS) {
                throw new IllegalStateException("too much backtracking!");
            }
            return wrapped.charAt(index);
        }

        @Override
        public @NotNull CharSequence subSequence(int start, int end) {
            return wrapped.subSequence(start, end);
        }
    }
}
