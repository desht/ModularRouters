package me.desht.modularrouters.util;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps track of the module & filter slots currently being worked on by a player.
 */
public class SlotTracker {
    private static final SlotTracker clientInstance = new SlotTracker();
    private static final SlotTracker serverInstance = new SlotTracker();
    private final Map<UUID,SlotTracker> handlers = new HashMap<>();

    private int moduleSlot;
    private int filterSlot;

    private static SlotTracker getManagerInstance(EntityPlayer player) {
        return player.world.isRemote ? clientInstance : serverInstance;
    }

    public static SlotTracker getInstance(EntityPlayer player) {
        return getManagerInstance(player).handlers.computeIfAbsent(player.getUniqueID(), v -> new SlotTracker());
    }

    private SlotTracker() {
        clearSlots();
    }

    public void clearModuleSlot() {
        setModuleSlot(-1);
    }

    public void clearFilterSlot() {
        setFilterSlot(-1);
    }

    public void clearSlots() {
        clearModuleSlot();
        clearFilterSlot();
    }

    public int getModuleSlot() {
        return moduleSlot;
    }

    public int getFilterSlot() {
        return filterSlot;
    }

    public void setModuleSlot(int moduleSlot) {
        this.moduleSlot = moduleSlot;
    }

    public void setFilterSlot(int filterSlot) {
        this.filterSlot = filterSlot;
    }

    public ItemStack getConfiguringModule(TileEntityItemRouter router) {
        return moduleSlot >= 0 ? router.getModules().getStackInSlot(moduleSlot) : ItemStack.EMPTY;
    }

    public ItemStack getConfiguringFilter(TileEntityItemRouter router) {
        return getConfiguringFilter(getConfiguringModule(router));
    }

    public ItemStack getConfiguringFilter(ItemStack moduleStack) {
        return filterSlot >= 0 && moduleStack.getItem() instanceof ItemModule ?
                new BaseModuleHandler.ModuleFilterHandler(moduleStack).getStackInSlot(filterSlot) : ItemStack.EMPTY;
    }
}
