package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.base.Joiner;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
            if (comp.matches(stack)) {
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

    public static class Comparison {
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

        boolean matches(ItemStack stack) {
            if (op == null || subject == null) {
                return false;
            }
            Optional<Integer> val = subject.getValue(stack);
            return op.check(val.orElse(0), target);
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

        @OnlyIn(Dist.CLIENT)
        public String asLocalizedText() {
            if (subject == null || op == null) return "<?>";
            return Joiner.on(" ").join(
                    I18n.format("guiText.label.inspectionSubject." + subject),
                    I18n.format("guiText.label.inspectionOp." + op),
                    target + subject.getDisplaySuffix()
            );
        }
    }

    public enum InspectionSubject {
        NONE,
        DURABILITY,
        FLUID,
        ENERGY,
        ENCHANT,
        FOOD;

        private Optional<Integer> getValue(ItemStack stack) {
            switch (this) {
                case NONE:
                    return Optional.empty();
                case DURABILITY:
                    return stack.getMaxDamage() > 0 ?
                            Optional.of(asPercentage(stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage())) :
                            Optional.empty();
                case FLUID:
                    return getFluidPercent(stack);
                case ENERGY:
                    return getEnergyPercent(stack);
                case ENCHANT:
                    return getHighestEnchantLevel(stack);
                case FOOD:
                    return getFoodValue(stack);
                default:
                    throw new IllegalArgumentException("invalid comparison subject! " + this);
            }
        }

        private Optional<Integer> getFoodValue(ItemStack stack) {
            if (stack.getItem().isFood()) {
                return Optional.of(stack.getItem().getFood().getHealing());
            } else {
                return Optional.empty();
            }
        }

        private Optional<Integer> getHighestEnchantLevel(ItemStack stack) {
            return EnchantmentHelper.getEnchantments(stack).values().stream().max(Comparator.naturalOrder());
        }

        private Optional<Integer> getEnergyPercent(ItemStack stack) {
            return stack.getCapability(CapabilityEnergy.ENERGY, null)
                    .map(handler -> Optional.of(asPercentage(handler.getEnergyStored(), handler.getMaxEnergyStored())))
                    .orElse(Optional.empty());

//            if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
//                IEnergyStorage s = stack.getCapability(CapabilityEnergy.ENERGY, null);
//                return Optional.of(asPercentage(s.getEnergyStored(), s.getMaxEnergyStored()));
//            } else if (TeslaIntegration.enabled && TeslaUtils.isTeslaHolder(stack, null)) {
//                ITeslaHolder h = TeslaUtils.getTeslaHolder(stack, null);
//                return Optional.of(asPercentage(h.getStoredPower(), h.getCapacity()));
//            } else if (RFIntegration.enabled && stack.getItem() instanceof IEnergyContainerItem) {
//                IEnergyContainerItem containerItem = (IEnergyContainerItem) stack.getItem();
//                return Optional.of(asPercentage(containerItem.getEnergyStored(stack), containerItem.getMaxEnergyStored(stack)));
//            }
//            return Optional.empty();
        }

        private Optional<Integer> getFluidPercent(ItemStack stack) {
            return FluidUtil.getFluidHandler(stack)
                    .map(handler -> {
                        int total = 0;
                        int max = 0;
                        for (IFluidTankProperties p : handler.getTankProperties()) {
                            FluidStack fluidStack = p.getContents();
                            max += p.getCapacity();
                            if (fluidStack != null) total += fluidStack.amount;
                        }
                        return Optional.of(asPercentage(total, max));
                    })
                    .orElse(Optional.empty());
        }

        public String getDisplaySuffix() {
            switch (this) {
                case ENCHANT:case FOOD:case NONE:
                    return "";
                default:
                    return "%";
            }
        }

        public InspectionSubject cycle(int direction) {
            int n = this.ordinal() + direction;
            if (n >= values().length) n = 0;
            else if (n < 0) n = values().length - 1;
            return values()[n];
        }

        private static final BigDecimal HUNDRED = new BigDecimal(100);
        private int asPercentage(long val, long max) {
            // BigDecimal is a bit overkill perhaps, but guarantees no danger of overflow here
            BigDecimal a = new BigDecimal(val);
            BigDecimal b = new BigDecimal(max);
            return a.multiply(HUNDRED).divide(b, BigDecimal.ROUND_DOWN).intValue();
        }
    }

    public enum InspectionOp {
        NONE,
        GT,
        LT,
        LE,
        GE,
        EQ,
        NE;

        public boolean check(long value, long target) {
            switch (this) {
                case NONE:
                    return false;
                case GT:
                    return value > target;
                case LT:
                    return value < target;
                case LE:
                    return value <= target;
                case GE:
                    return value >= target;
                case EQ:
                    return value == target;
                case NE:
                    return value != target;
            }
            return false;
        }

        public InspectionOp cycle(int direction) {
            int n = this.ordinal() + direction;
            if (n >= values().length) n = 0;
            else if (n < 0) n = values().length - 1;
            return values()[n];
        }
    }
}
