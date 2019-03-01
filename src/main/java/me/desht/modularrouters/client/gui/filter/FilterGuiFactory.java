package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FilterGuiFactory {
    /**
     * Create a GUI for a filter item in the player's hand.
     *
     * @param player the player
     * @param hand hand the filter item is in
     * @return the gui
     */
    public static GuiScreen createGui(EntityPlayer player, EnumHand hand) {
        return createGui(player, player.getHeldItem(hand), null, hand);
    }

    /**
     * Create a GUI for the filter in a module which is installed in an item router. The module & filter slot indexes
     * are already stored in the router.
     *
     * @param player the player
     * @param router the item router
     * @return the gui
     */
    public static GuiScreen createGui(EntityPlayer player, TileEntityItemRouter router) {
        ItemStack filterStack = SlotTracker.getInstance(player).getConfiguringFilter(router);
        if (filterStack.getItem() instanceof ItemSmartFilter) {
            return createGui(player, filterStack, router, null);
        }
        return null;
    }

    private static GuiScreen createGui(EntityPlayer player, ItemStack heldStack, TileEntityItemRouter router, EnumHand hand) {
        final ItemStack filterStack;
        if (heldStack.getItem() instanceof ItemModule) {
            filterStack = SlotTracker.getInstance(player).getConfiguringFilter(heldStack);
            SlotTracker.getInstance(player).clearFilterSlot();
        } else if (heldStack.getItem() instanceof ItemSmartFilter) {
            filterStack = heldStack;
        } else {
            return null;
        }

        if (filterStack.isEmpty()) {
            return null;
        }

        try {
            ItemSmartFilter filter = (ItemSmartFilter) filterStack.getItem();
            Class<? extends GuiScreen> clazz = filter.getGuiClass();
            if (filter.hasContainer()) {
                Constructor<? extends GuiScreen> ctor = clazz.getConstructor(ContainerSmartFilter.class);
                ContainerSmartFilter container = filter.createContainer(player, hand, router);
                return ctor.newInstance(container);
            } else {
                Constructor<? extends GuiScreen> ctor = clazz.getConstructor(ItemStack.class, TileEntityItemRouter.class, EnumHand.class);
                return ctor.newInstance(filterStack, router, hand);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
