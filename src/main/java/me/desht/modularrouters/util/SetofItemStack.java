package me.desht.modularrouters.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import me.desht.modularrouters.logic.settings.ModuleFlags;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;

import java.util.Collection;
import java.util.Comparator;

public class SetofItemStack extends ObjectOpenCustomHashSet<ItemStack> {
    private record ItemStackHashingStrategy(ModuleFlags filterFlags) implements Strategy<ItemStack> {
        @Override
        public int hashCode(ItemStack object) {
            if (filterFlags.matchComponents()) {
                return ItemStack.hashItemAndComponents(object);
            }

            int hashCode = Item.getId(object.getItem());
            return filterFlags.matchDamage() ? hashCode + 37 * object.getDamageValue() : hashCode;
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            return (o1 == o2) || !(o1 == null || o2 == null)
                    && o1.getItem() == o2.getItem()
                    && (!filterFlags.matchDamage() || o1.getDamageValue() == o2.getDamageValue())
                    && (!filterFlags.matchComponents() || ItemStack.isSameItemSameComponents(o1, o2));
        }
    }

    public SetofItemStack(ModuleFlags filterFlags) {
        super(new ItemStackHashingStrategy(filterFlags));
    }

    public SetofItemStack(Collection<? extends ItemStack> collection, ModuleFlags filterFlags) {
        super(collection, new ItemStackHashingStrategy(filterFlags));
    }

    public static SetofItemStack fromResourceHandler(ResourceHandler<ItemResource> handler, ModuleFlags filterFlags) {
        NonNullList<ItemStack> itemStacks = NonNullList.create();
        for (int i = 0; i < handler.size(); i++) {
            ItemStack stack = ItemUtil.getStack(handler, i);
            if (!stack.isEmpty()) {
                itemStacks.add(stack.copy());
            }
        }
        return new SetofItemStack(itemStacks, filterFlags);
    }

    public Collection<ItemStack> sorted() {
        return this.stream().sorted(COMPARE_STACKS).toList();
    }

    // sort by mod, then by display name
    private static final Comparator<? super ItemStack> COMPARE_STACKS = Comparator
            .comparing((ItemStack stack) -> namespace(stack.getItem()))
            .thenComparing(stack -> stack.getHoverName().getString());

    private static String namespace(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getNamespace();
    }
}
