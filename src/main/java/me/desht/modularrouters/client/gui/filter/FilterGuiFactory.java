package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class FilterGuiFactory {
    private static final Map<Item, BiFunction<ItemStack,MFLocator,? extends Screen>> REGISTRY = new HashMap<>();

    public static void registerGuiHandler(Item filter, BiFunction<ItemStack,MFLocator,? extends Screen> factory) {
        REGISTRY.put(filter, factory);
    }

    /**
     * Open a (non-container-based) GUI for a filter module.  Container-based GUI's are opened via the usual server-side
     * container mechanism.
     *
     * @param locator the filter location; could be in a player's hand, or in a module (which may or may not be in a router)
     */
    public static void openFilterGui(MFLocator locator) {
        ItemStack filterStack = locator.getTargetItem(Minecraft.getInstance().player);
        if (filterStack.getItem() instanceof ItemSmartFilter
                && !((ItemSmartFilter) filterStack.getItem()).hasContainer()
                && REGISTRY.containsKey(filterStack.getItem()))
        {
            Minecraft.getInstance().setScreen(REGISTRY.get(filterStack.getItem()).apply(filterStack, locator));
        }
    }
}
