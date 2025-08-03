package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.item.MRBaseItem;
import me.desht.modularrouters.item.module.ModuleItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AugmentItem extends MRBaseItem {
    public static final int SLOTS = 4;

    public AugmentItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void addExtraInformation(ItemStack stack, Consumer<Component> consumer) {
    }

    public abstract int getMaxAugments(ModuleItem moduleType);

    public Component getExtraInfo(int c, ItemStack moduleStack) {
        return Component.empty();
    }

    public static class AugmentCounter {
        private final Map<AugmentItem, Integer> counts = new HashMap<>();

        public AugmentCounter(ItemStack moduleStack) {
            refresh(moduleStack);
        }

        public void refresh(ItemStack moduleStack) {
            Validate.isTrue(moduleStack.getItem() instanceof ModuleItem, "item is not a ModuleItem: " + moduleStack);

            AugmentHandler h = new AugmentHandler(moduleStack, null);
            counts.clear();
            for (int i = 0; i < h.getSlots(); i++) {
                ItemStack augmentStack = h.getStackInSlot(i);
                if (augmentStack.getItem() instanceof AugmentItem augment) {
                    counts.merge(augment, augmentStack.getCount(), Integer::sum);
                }
            }
        }

        public Collection<AugmentItem> getAugments() {
            return counts.keySet().stream().toList();
        }

        public int getAugmentCount(Supplier<Item> type) {
            return getAugmentCount(type.get());
        }

        public int getAugmentCount(Item type) {
            return type instanceof AugmentItem ? counts.getOrDefault(type, 0) : 0;
        }
    }
}
