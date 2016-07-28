package me.desht.modularrouters.proxy;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.ItemRouterContainer;
import me.desht.modularrouters.container.ModuleContainer;
import me.desht.modularrouters.container.ModuleInventory;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.gui.GuiModule;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiProxy implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ModularRouters.GUI_MODULE) {
            return new ModuleContainer(player, new ModuleInventory(player.getHeldItem(EnumHand.MAIN_HAND)));
        } else if (ID == ModularRouters.GUI_ROUTER) {
            TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
            return tileEntity instanceof TileEntityItemRouter ? new ItemRouterContainer(player.inventory, (TileEntityItemRouter) tileEntity) : null;
        } else {
            return null;
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ModularRouters.GUI_MODULE) {
            return new GuiModule(new ModuleContainer(player, new ModuleInventory(player.getHeldItem(EnumHand.MAIN_HAND))));
        } else if (ID == ModularRouters.GUI_ROUTER) {
            TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
            return tileEntity instanceof TileEntityItemRouter ? new GuiItemRouter(player.inventory, (TileEntityItemRouter) tileEntity) : null;
        } else {
            return null;
        }
    }
}
