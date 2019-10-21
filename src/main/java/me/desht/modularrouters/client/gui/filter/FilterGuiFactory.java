package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FilterGuiFactory {
    /**
     * Create a (non-container-based) GUI for a filter module.
     *
     * @param locator the filter location; could be in a player's hand, or in a module (which may or may not be in a router)
     * @return the gui for this type of filter
     */
    public static Screen createGuiForFilter(MFLocator locator) {
        ItemStack filterStack = locator.getTargetItem(Minecraft.getInstance().player);

        if (filterStack.getItem() instanceof ItemSmartFilter && !((ItemSmartFilter)filterStack.getItem()).hasContainer()) {
            try {
                ItemSmartFilter filter = (ItemSmartFilter) filterStack.getItem();
                Class<? extends Screen> clazz = filter.getGuiClass();
                Constructor<? extends Screen> ctor = clazz.getConstructor(ItemStack.class, MFLocator.class);
                return ctor.newInstance(filterStack, locator);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static void openFilterGui(MFLocator locator) {
        Minecraft.getInstance().displayGuiScreen(createGuiForFilter(locator));
    }
}
