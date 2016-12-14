package me.desht.modularrouters.gui;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ContainerItemRouter;
import me.desht.modularrouters.gui.filter.FilterGuiFactory;
import me.desht.modularrouters.gui.module.ModuleGuiFactory;
import me.desht.modularrouters.gui.upgrade.GuiSyncUpgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ModularRouters.GUI_ROUTER) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, new BlockPos(x, y, z));
            return router != null ? new ContainerItemRouter(player.inventory, router) : null;
        } else if (ID == ModularRouters.GUI_MODULE_HELD_MAIN) {
            return ModuleGuiFactory.createContainer(player, EnumHand.MAIN_HAND);
        } else if (ID == ModularRouters.GUI_MODULE_HELD_OFF) {
            return ModuleGuiFactory.createContainer(player, EnumHand.OFF_HAND);
        } else if (ID == ModularRouters.GUI_MODULE_INSTALLED) {
            return ModuleGuiFactory.createContainer(player, world, x, y, z);
        } else if (ID == ModularRouters.GUI_FILTER_HELD_MAIN) {
            return FilterGuiFactory.createContainer(player, EnumHand.MAIN_HAND);
        } else if (ID == ModularRouters.GUI_FILTER_HELD_OFF) {
            return FilterGuiFactory.createContainer(player, EnumHand.OFF_HAND);
        } else if (ID == ModularRouters.GUI_FILTER_INSTALLED) {
            return FilterGuiFactory.createContainer(player, world, x, y, z);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ModularRouters.GUI_ROUTER) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, new BlockPos(x, y, z));
            return router != null ? new GuiItemRouter(player.inventory, router) : null;
        } else if (ID == ModularRouters.GUI_MODULE_HELD_MAIN) {
            return ModuleGuiFactory.createGui(player, EnumHand.MAIN_HAND);
        } else if (ID == ModularRouters.GUI_MODULE_HELD_OFF) {
            return ModuleGuiFactory.createGui(player, EnumHand.OFF_HAND);
        } else if (ID == ModularRouters.GUI_MODULE_INSTALLED) {
            return ModuleGuiFactory.createGui(player, world, x, y, z);
        } else if (ID == ModularRouters.GUI_FILTER_HELD_MAIN) {
            return FilterGuiFactory.createGui(player, EnumHand.MAIN_HAND);
        } else if (ID == ModularRouters.GUI_FILTER_HELD_OFF) {
            return FilterGuiFactory.createGui(player, EnumHand.OFF_HAND);
        } else if (ID == ModularRouters.GUI_FILTER_INSTALLED) {
            return FilterGuiFactory.createGui(player, world, x, y, z);
        } else if (ID == ModularRouters.GUI_SYNC_UPGRADE) {
            return new GuiSyncUpgrade(player.getHeldItem(EnumHand.MAIN_HAND));
        }
        return null;
    }
}
