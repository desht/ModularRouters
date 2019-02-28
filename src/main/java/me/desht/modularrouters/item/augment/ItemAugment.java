package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ItemAugment extends ItemBase {
    public static final int SLOTS = 4;

    public ItemAugment(Properties props) {
        super(props);
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<ITextComponent> list) {
    }

    public abstract boolean isCompatible(ItemModule moduleType);

    public String getExtraInfo(int c, ItemStack moduleStack) {
        return "";
    }

    public static class AugmentCounter {
        private final Map<ResourceLocation, Integer> counts = new HashMap<>();

        public AugmentCounter(ItemStack moduleStack) {
            if (!(moduleStack.getItem() instanceof ItemModule)) {
                throw new IllegalArgumentException("item is not a ItemModule: " + moduleStack);
            }

            AugmentHandler h = new AugmentHandler(moduleStack);
            for (int i = 0; i < h.getSlots(); i++) {
                ItemStack augmentStack = h.getStackInSlot(i);
                if (augmentStack.getItem() instanceof ItemAugment) {
                    ResourceLocation k = augmentStack.getItem().getRegistryName();
                    counts.put(k, counts.getOrDefault(k, 0) + augmentStack.getCount());
                }
            }
        }

        public Collection<ItemAugment> getAugments() {
            return counts.keySet().stream().map(ForgeRegistries.ITEMS::getValue).map(i -> (ItemAugment)i).collect(Collectors.toList());
        }

        public int getAugmentCount(Item type) {
            if (type == null) return 0;
            return counts.getOrDefault(type.getRegistryName(), 0);
        }
    }
}
