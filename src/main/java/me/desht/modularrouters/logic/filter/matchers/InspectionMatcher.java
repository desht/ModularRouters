package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.base.Joiner;
import me.desht.modularrouters.client.util.IHasTranslationKey;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class InspectionMatcher implements IItemMatcher {
    private final ComparisonList comparisonList;

    public InspectionMatcher(ComparisonList comparisons) {
        this.comparisonList = comparisons;
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        int matched = 0;
        if (comparisonList.items.isEmpty()) {
            return false;
        }
        for (Comparison comp : comparisonList.items) {
            if (comp.test(stack)) {
                if (!comparisonList.matchAll) {
                    return true;
                } else {
                    matched++;
                }
            }
        }
        return matched >= comparisonList.items.size();
    }

    public static class ComparisonList {
        public final List<Comparison> items;
        boolean matchAll;

        public ComparisonList(List<Comparison> items, boolean matchAll) {
            this.items = items;
            this.matchAll = matchAll;
        }

        public void setMatchAll(boolean matchAll) {
            this.matchAll = matchAll;
        }

        public boolean isMatchAll() {
            return matchAll;
        }
    }

    public static class Comparison implements Predicate<ItemStack> {
        static final Comparison BAD_COMPARISON = new Comparison();

        private final InspectionSubject subject;
        private final InspectionOp op;
        private final long target;

        Comparison(InspectionSubject subject, InspectionOp op, int target) {
            this.subject = subject;
            this.op = op;
            this.target = target;
        }

        Comparison() {
            subject = null;
            op = null;
            target = 0;
        }

        @Override
        public boolean test(ItemStack stack) {
            if (op == null || subject == null) {
                return false;
            }
            Optional<Integer> val = subject.evaluator.apply(stack);
            return op.test(Long.valueOf(val.orElse(-1)), target);
        }

        public static Comparison fromString(String s) {
            String[] fields = s.split(" ", 3);
            if (fields.length != 3) return BAD_COMPARISON;
            try {
                InspectionSubject subject = InspectionSubject.valueOf(fields[0]);
                InspectionOp op = InspectionOp.valueOf(fields[1]);
                int target = Integer.parseInt(fields[2]);
                return new Comparison(subject, op, target);
            } catch (IllegalArgumentException e) {
                return BAD_COMPARISON;
            }
        }

        @Override
        public String toString() {
            return Joiner.on(" ").join(subject, op, target);
        }

        public Component asLocalizedText() {
            if (subject == null || op == null) return Component.literal("<?>");
            return Component.literal(" ")
                    .append(Component.translatable("modularrouters.guiText.label.inspectionSubject." + subject))
                    .append(" ")
                    .append(Component.translatable("modularrouters.guiText.label.inspectionOp." + op))
                    .append(target + subject.suffix);
        }
    }

    public enum InspectionSubject implements IHasTranslationKey {
        NONE("", stack -> Optional.empty()),
        DURABILITY("%", InspectionSubject::getDurabilityPercent),
        FLUID("%", InspectionSubject::getFluidPercent),
        ENERGY("%", InspectionSubject::getEnergyPercent),
        ENCHANT("", InspectionSubject::getHighestEnchantLevel),
        FOOD("", InspectionSubject::getFoodValue);

        private final String suffix;
        private final Function<ItemStack, Optional<Integer>> evaluator;

        InspectionSubject(String suffix, Function<ItemStack, Optional<Integer>> evaluator) {
            this.suffix = suffix;
            this.evaluator = evaluator;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.guiText.label.inspectionSubject." + this;
        }

        private static Optional<Integer> getDurabilityPercent(ItemStack stack) {
            return stack.getMaxDamage() > 0 ?
                    Optional.of(asPercentage(stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage())) :
                    Optional.empty();
        }

        private static Optional<Integer> getFoodValue(ItemStack stack) {
            //noinspection ConstantConditions
            return stack.getItem().isEdible() ?
                    Optional.of(stack.getItem().getFoodProperties(stack, null).getNutrition()) :
                    Optional.empty();
        }

        private static Optional<Integer> getHighestEnchantLevel(ItemStack stack) {
            return EnchantmentHelper.getEnchantments(stack).values().stream().max(Comparator.naturalOrder());
        }

        private static Optional<Integer> getEnergyPercent(ItemStack stack) {
            return stack.getCapability(Capabilities.ENERGY, null)
                    .map(handler -> Optional.of(asPercentage(handler.getEnergyStored(), handler.getMaxEnergyStored())))
                    .orElse(Optional.empty());
        }

        private static Optional<Integer> getFluidPercent(ItemStack stack) {
            return FluidUtil.getFluidHandler(stack)
                    .map(handler -> {
                        int total = 0;
                        int max = 0;
                        for (int idx = 0; idx < handler.getTanks(); idx++) {
                            max += handler.getTankCapacity(idx);
                            total += handler.getFluidInTank(idx).getAmount();
                        }
                        return Optional.of(asPercentage(total, max));
                    })
                    .orElse(Optional.empty());
        }

        public InspectionSubject cycle(int direction) {
            int n = this.ordinal() + direction;
            if (n >= values().length) n = 0;
            else if (n < 0) n = values().length - 1;
            return values()[n];
        }

        private static final BigDecimal HUNDRED = new BigDecimal(100);
        private static int asPercentage(long val, long max) {
            if (max == 0) return 0;  // https://github.com/desht/ModularRouters/issues/82
            // BigDecimal is a bit overkill perhaps, but guarantees no danger of overflow here
            BigDecimal a = new BigDecimal(val);
            BigDecimal b = new BigDecimal(max);
            return a.multiply(HUNDRED).divide(b, RoundingMode.DOWN).intValue();
        }
    }

    public enum InspectionOp implements IHasTranslationKey, BiPredicate<Long,Long> {
        NONE((val, target) -> false),
        GT((val, target) -> val > target),
        LT((val, target) -> val < target),
        LE((val, target) -> val <= target),
        GE((val, target) -> val >= target),
        EQ(Objects::equals),
        NE((val, target) -> !Objects.equals(val, target));

        private final BiPredicate<Long,Long> predicate;

        InspectionOp(BiPredicate<Long,Long> predicate) {
            this.predicate = predicate;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.guiText.label.inspectionOp." + this;
        }

        @Override
        public boolean test(Long value, Long target) {
            return predicate.test(value, target);
        }

        public InspectionOp cycle(int direction) {
            int n = this.ordinal() + direction;
            if (n >= values().length) n = 0;
            else if (n < 0) n = values().length - 1;
            return values()[n];
        }
    }
}
