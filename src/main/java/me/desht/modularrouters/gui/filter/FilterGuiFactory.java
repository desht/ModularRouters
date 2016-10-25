package me.desht.modularrouters.gui.filter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.FilterContainer;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.SmartFilter;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FilterGuiFactory {
    public static GuiScreen createGui(EntityPlayer player, EnumHand hand) {
        return createGui(player, player.getHeldItem(hand), null, -1, -1, hand);
    }

    public static GuiScreen createGui(EntityPlayer player, World world, int x, int y, int z) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, new BlockPos(x, y, z));
        if (router != null) {
            int moduleIndex = router.getModuleConfigSlot(player);
            int filterIndex = router.getFilterConfigSlot(player);
            ItemStack filterStack = getFilterStackInInstalledModule(router, moduleIndex, filterIndex);
            if (ItemSmartFilter.getFilter(filterStack) != null) {
                router.clearConfigSlot(player);
                return FilterGuiFactory.createGui(player, filterStack, router.getPos(), moduleIndex, filterIndex, null);
            }
        }
        return null;
    }

    private static GuiScreen createGui(EntityPlayer player, ItemStack heldStack, BlockPos routerPos, int moduleSlotIndex, int filterSlotIndex, EnumHand hand) {
        ItemStack filterStack;
        if (ItemModule.getModule(heldStack) != null) {
            filterSlotIndex = ItemModule.getFilterConfigSlot(heldStack);
            filterStack = getFilterStackInUninstalledModule(heldStack, filterSlotIndex);
            ItemModule.setFilterConfigSlot(heldStack, -1);
        } else if (ItemSmartFilter.getFilter(heldStack) != null) {
            filterStack = heldStack;
        } else {
            return null;
        }

        if (filterStack == null) {
            return null;
        }

        try {
            SmartFilter filter = ItemSmartFilter.getFilter(filterStack);
            Class<? extends GuiScreen> clazz = filter.getGuiHandler();
            if (filter.hasGuiContainer()) {
                Constructor<? extends GuiScreen> ctor = clazz.getConstructor(FilterContainer.class, BlockPos.class, Integer.class, Integer.class, EnumHand.class);
                TileEntityItemRouter router = routerPos == null ? null : TileEntityItemRouter.getRouterAt(player.getEntityWorld(), routerPos);
                return ctor.newInstance(createContainer(player, filterStack, router), routerPos, moduleSlotIndex, filterSlotIndex, hand);
            } else {
                Constructor<? extends GuiScreen> ctor = clazz.getConstructor(ItemStack.class, BlockPos.class, Integer.class, Integer.class, EnumHand.class);
                return ctor.newInstance(filterStack, routerPos, moduleSlotIndex, filterSlotIndex, hand);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Container createContainer(EntityPlayer player, ItemStack filterStack) {
        return createContainer(player, filterStack, null);
    }

    public static Container createContainer(EntityPlayer player, World world, int x, int y, int z) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, new BlockPos(x, y, z));
        if (router != null) {
            int moduleIndex = router.getModuleConfigSlot(player);
            int filterIndex = router.getFilterConfigSlot(player);
            ItemStack filterStack = getFilterStackInInstalledModule(router, moduleIndex, filterIndex);
            return createContainer(player, filterStack, router);
        }
        return null;
    }

    private static Container createContainer(EntityPlayer player, ItemStack stack, TileEntityItemRouter router) {
        SmartFilter f = ItemSmartFilter.getFilter(stack);
        return f != null && f.hasGuiContainer() ? f.createContainer(player, stack, router) : null;
    }

    private static ItemStack getFilterStackInUninstalledModule(ItemStack moduleStack, int filterIdx) {
        return getStackInSlot(moduleStack, filterIdx);
    }

    private static ItemStack getFilterStackInInstalledModule(TileEntityItemRouter router, int moduleIdx, int filterIdx) {
        if (moduleIdx < 0 || filterIdx < 0) {
            return null;
        }
        ItemStack installedModuleStack = router.getModules().getStackInSlot(moduleIdx);
        return getStackInSlot(installedModuleStack, filterIdx);
    }

    private static ItemStack getStackInSlot(ItemStack moduleStack, int filterIdx) {
        NBTTagCompound compound = moduleStack.getTagCompound();
        if (compound != null) {
            NBTTagList items = compound.getTagList(Filter.NBT_FILTER, Constants.NBT.TAG_COMPOUND);
            NBTBase b = items.get(filterIdx);
            if (b instanceof NBTTagCompound) {
                return ItemStack.loadItemStackFromNBT((NBTTagCompound) b);
            } else {
                ModularRouters.logger.warn("can't find filter item in module: index=" + filterIdx);
            }
        }
        return null;
    }

}
