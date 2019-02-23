package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ModuleGuiFactory {
    /**
     * Create a module GUI for a held item module.  This is called via ItemModule when the player right-clicks a
     * module item in hand.
     *
     * @param player the player
     * @param hand the hand the module item is in
     * @return the gui
     */
    public static GuiModule createGui(EntityPlayer player, EnumHand hand) {
        return createGui(player, player.getHeldItem(hand), null, hand);
    }

    /**
     * Create a module GUI for a module which is installed in an item router. This is called via the OpenGuiMessage
     * packet when the player presses 'C' or middle-clicks a module in a router GUI.
     *
     * @param player the player
     * @param router the item router
     * @return the gui
     */
    public static GuiModule createGui(EntityPlayer player, TileEntityItemRouter router) {
//        ItemStack moduleStack = router.getConfiguringModule(player);
        ItemStack moduleStack = SlotTracker.getInstance(player).getConfiguringModule(router);
        return createGui(player, moduleStack, router, null);
    }

    private static GuiModule createGui(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router, EnumHand hand) {
        if (!(moduleStack.getItem() instanceof ItemModule))
            return null;

        Class<? extends GuiModule> clazz = ((ItemModule) moduleStack.getItem()).getGuiClass();
        try {
            Constructor<? extends GuiModule> ctor = clazz.getConstructor(ContainerModule.class);
            ContainerModule container = ((ItemModule) moduleStack.getItem()).createContainer(player, hand, moduleStack, router);
            return ctor.newInstance(container);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
