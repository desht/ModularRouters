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
//        ItemStack filterStack = router.getConfiguringFilter(player);
        if (filterStack.getItem() instanceof ItemSmartFilter) {
            SlotTracker.getInstance(player).clearSlots();
//            router.clearConfigSlot(player);
            return createGui(player, filterStack, router, null);
        }
        return null;
    }

    private static GuiScreen createGui(EntityPlayer player, ItemStack heldStack, TileEntityItemRouter router, EnumHand hand) {
        final ItemStack filterStack;
        if (heldStack.getItem() instanceof ItemModule) {
            filterStack = SlotTracker.getInstance(player).getConfiguringFilter(heldStack);
            SlotTracker.getInstance(player).clearFilterSlot();
//            filterSlotIndex = ModuleHelper.getFilterConfigSlot(heldStack);
//            filterStack = getFilterStackInUninstalledModule(heldStack, filterSlotIndex);
//            ModuleHelper.setFilterConfigSlot(heldStack, -1);
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
                Constructor<? extends GuiScreen> ctor = clazz.getConstructor(ItemStack.class, BlockPos.class, EnumHand.class);
                return ctor.newInstance(filterStack, router.getPos(), hand);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

//    /**
//     * Create a container for a filter held in the player's hand (or in a module held in a player's hand)
//     *
//     * @param player the player
//     * @param hand hand in which the item is held
//     * @return the container object
//     */
//    public static Container createContainer(EntityPlayer player, EnumHand hand) {
//        ItemStack heldStack = player.getHeldItem(hand);
//        if (heldStack.getItem() instanceof ItemModule) {
//            // filter is in a module in player's hand
//            int filterIndex = ModuleHelper.getFilterConfigSlot(heldStack);
//            if (filterIndex >= 0) {
//                ItemStack filterStack = getFilterStackInUninstalledModule(heldStack, filterIndex);
//                ModuleHelper.setFilterConfigSlot(heldStack, -1);
//                return createContainer(player, filterStack, hand, null);
//            }
//        } else {
//            // filter is held directly in player's hand
//            return createContainer(player, heldStack, hand, null);
//        }
//        return null;
//    }
//
//    /**
//     * Create a container for a filter in a module which is installed in a router.
//     *
//     * @param player the player
//     * @param world world the router is in
//     * @param x router's X position
//     * @param y router's Y position
//     * @param z router's Z position
//     * @return the container object
//     */
//    public static Container createContainer(EntityPlayer player, World world, int x, int y, int z) {
//        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, new BlockPos(x, y, z));
//        if (router != null) {
//            int moduleIndex = router.getModuleConfigSlot(player);
//            int filterIndex = router.getFilterConfigSlot(player);
//            ItemStack filterStack = getFilterStackInInstalledModule(router, moduleIndex, filterIndex);
//            return createContainer(player, filterStack, null, router);
//        }
//        return null;
//    }
//
//    private static Container createContainer(EntityPlayer player, ItemStack stack, EnumHand hand, TileEntityItemRouter router) {
//        if (stack.getItem() instanceof ItemSmartFilter) {
//            ItemSmartFilter f = (ItemSmartFilter) stack.getItem();
//            return f.hasGuiContainer() ? f.createContainer(player, hand, stack, router) : null;
//        } else {
//            return null;
//        }
//    }

    @Nonnull
    private static ItemStack getFilterStackInUninstalledModule(ItemStack moduleStack, int filterIdx) {
        return getStackInSlot(moduleStack, filterIdx);
    }

    @Nonnull
    private static ItemStack getFilterStackInInstalledModule(TileEntityItemRouter router, int moduleIdx, int filterIdx) {
        if (moduleIdx < 0 || filterIdx < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack installedModuleStack = router.getModules().getStackInSlot(moduleIdx);
        return getStackInSlot(installedModuleStack, filterIdx);
    }

    @Nonnull
    private static ItemStack getStackInSlot(ItemStack moduleStack, int filterIdx) {
        ModuleFilterHandler handler = new ModuleFilterHandler(moduleStack);
        ItemStack stack = handler.getStackInSlot(filterIdx);
        if (!stack.isEmpty()) {
            return stack;
        } else {
            ModularRouters.LOGGER.warn("can't find filter item in module '" + moduleStack + "', slot " + filterIdx);
            return ItemStack.EMPTY;
        }
    }
}
