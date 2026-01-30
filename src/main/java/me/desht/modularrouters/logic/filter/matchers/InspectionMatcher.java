package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.base.Joiner;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.api.matching.IModuleFlags;
import me.desht.modularrouters.util.TranslatableEnum;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class InspectionMatcher implements IItemMatcher {
    private final ComparisonList comparisonList;

    public InspectionMatcher(ComparisonList comparisons) {
        this.comparisonList = comparisons;
    }

    @Override
    public boolean matchItem(ItemStack stack, IModuleFlags flags) {
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

    public record ComparisonList(List<Comparison> items, boolean matchAll) {
        public static final ComparisonList DEFAULT = new ComparisonList(List.of(), false);

        public static final Codec<ComparisonList> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Comparison.CODEC.listOf().fieldOf("items").forGetter(ComparisonList::items),
                Codec.BOOL.fieldOf("match_all").forGetter(ComparisonList::matchAll)
        ).apply(builder, ComparisonList::new));

        public static final StreamCodec<FriendlyByteBuf, ComparisonList> STREAM_CODEC = StreamCodec.composite(
                Comparison.STREAM_CODEC.apply(ByteBufCodecs.list()), ComparisonList::items,
                ByteBufCodecs.BOOL, ComparisonList::matchAll,
                ComparisonList::new
        );

        public List<Comparison> items() {
            return Collections.unmodifiableList(items);
        }

        public boolean isEmpty() {
            return items.isEmpty();
        }

        public ComparisonList setMatchAll(boolean matchAll) {
            return new ComparisonList(items(), matchAll);
        }

        public ComparisonList addComparison(Comparison toAdd) {
            List<Comparison> l = new ArrayList<>(items);
            l.add(toAdd);
            return new ComparisonList(List.copyOf(l), matchAll);
        }

        public ComparisonList removeAt(int pos) {
            List<Comparison> l = new ArrayList<>(items);
            if (pos >= 0 && pos < l.size()) {
                l.remove(pos);
            }
            return new ComparisonList(List.copyOf(l), matchAll);
        }
    }

    public record Comparison(InspectionSubject subject, InspectionOp op, int target) implements Predicate<ItemStack> {
        public static final Codec<Comparison> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                InspectionSubject.CODEC.fieldOf("subject").forGetter(Comparison::subject),
                InspectionOp.CODEC.fieldOf("op").forGetter(Comparison::op),
                Codec.INT.fieldOf("target").forGetter(Comparison::target)
        ).apply(builder, Comparison::new));

        public static final StreamCodec<FriendlyByteBuf, Comparison> STREAM_CODEC = StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(InspectionSubject.class), Comparison::subject,
                NeoForgeStreamCodecs.enumCodec(InspectionOp.class), Comparison::op,
                ByteBufCodecs.INT, Comparison::target,
                Comparison::new
        );

        @Override
        public boolean test(ItemStack stack) {
            if (op == null || subject == null) {
                return false;
            }
            Optional<Integer> val = subject.evaluator.apply(stack);
            return op.test(Long.valueOf(val.orElse(-1)), (long) target);
        }

        @Override
        public String toString() {
            return Joiner.on(" ").join(subject, op, target);
        }

        public MutableComponent asLocalizedText() {
            if (subject == null || op == null) return Component.literal("<?>");
            return xlate(subject.getTranslationKey())
                    .append(" ")
                    .append(xlate(op.getTranslationKey()))
                    .append(target + subject.suffix);
        }
    }

    public enum InspectionSubject implements TranslatableEnum, StringRepresentable {
        NONE("none", "", stack -> Optional.empty()),
        DURABILITY("durability", "%", InspectionSubject::getDurabilityPercent),
        FLUID("fluid", "%", InspectionSubject::getFluidPercent),
        ENERGY("energy","%", InspectionSubject::getEnergyPercent),
        ENCHANT("enchant", "", InspectionSubject::getHighestEnchantLevel),
        FOOD("food","", InspectionSubject::getFoodValue);

        public static final Codec<InspectionSubject> CODEC = StringRepresentable.fromEnum(InspectionSubject::values);

        private final String name;
        private final String suffix;
        private final Function<ItemStack, Optional<Integer>> evaluator;

        InspectionSubject(String name, String suffix, Function<ItemStack, Optional<Integer>> evaluator) {
            this.name = name;
            this.suffix = suffix;
            this.evaluator = evaluator;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.guiText.label.inspectionSubject." + name;
        }

        private static Optional<Integer> getDurabilityPercent(ItemStack stack) {
            return stack.getMaxDamage() > 0 ?
                    Optional.of(asPercentage(stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage())) :
                    Optional.empty();
        }

        private static Optional<Integer> getFoodValue(ItemStack stack) {
            //noinspection ConstantConditions
            return stack.has(DataComponents.FOOD) ?
                    Optional.of(stack.get(DataComponents.FOOD).nutrition()) :
                    Optional.empty();
        }

        private static Optional<Integer> getHighestEnchantLevel(ItemStack stack) {
            return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet().stream()
                    .map(Object2IntMap.Entry::getIntValue)
                    .max(Comparator.naturalOrder());
        }

        private static Optional<Integer> getEnergyPercent(ItemStack stack) {
            EnergyHandler storage = stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack));
            return storage == null ? Optional.empty() : Optional.of(asPercentage(storage.getAmountAsInt(), storage.getCapacityAsInt()));
        }

        private static Optional<Integer> getFluidPercent(ItemStack stack) {
            ResourceHandler<FluidResource> capability = stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(stack));
            if (capability == null) {
                return Optional.empty();
            }

            int total = 0;
            int max = 0;
            for (int idx = 0; idx < capability.size(); idx++) {
                max += capability.getCapacityAsInt(idx, capability.getResource(idx));
                total += capability.getAmountAsInt(idx);
            }

            return Optional.of(asPercentage(total, max));
        }

        public InspectionSubject cycle(int direction) {
            int n = this.ordinal() + direction;
            if (n >= values().length) n = 0;
            else if (n < 0) n = values().length - 1;
            return values()[n];
        }

        private static int asPercentage(int val, int max) {
            if (max == 0) return 0;  // https://github.com/desht/ModularRouters/issues/82
            return (int) ((val / (float) max) * 100);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum InspectionOp implements TranslatableEnum, StringRepresentable, BiPredicate<Long,Long> {
        NONE((val, target) -> false),
        GT((val, target) -> val > target),
        LT((val, target) -> val < target),
        LE((val, target) -> val <= target),
        GE((val, target) -> val >= target),
        EQ(Objects::equals),
        NE((val, target) -> !Objects.equals(val, target));

        public static final Codec<InspectionOp> CODEC = StringRepresentable.fromEnum(InspectionOp::values);

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

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
