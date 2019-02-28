package me.desht.modularrouters.client.gui;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.filter.FilterGuiFactory;
import me.desht.modularrouters.client.gui.module.ModuleGuiFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.FMLPlayMessages;

public class GuiHandler {

    public static GuiScreen openGui(FMLPlayMessages.OpenContainer msg) {
        EntityPlayer player = Minecraft.getInstance().player;
        PacketBuffer xtra = msg.getAdditionalData();
        switch (msg.getId().getPath()) {
            case "module_held":
                EnumHand hand = xtra.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                return ModuleGuiFactory.createGui(player, hand);
            case "module_installed":
                BlockPos routerPos = xtra.readBlockPos();
                return ModuleGuiFactory.createGui(player, TileEntityItemRouter.getRouterAt(Minecraft.getInstance().world, routerPos));
            case "filter_held":
                EnumHand hand1 = xtra.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                return FilterGuiFactory.createGui(player, hand1);
            case "filter_installed":
                BlockPos routerPos1 = xtra.readBlockPos();
                return FilterGuiFactory.createGui(player, TileEntityItemRouter.getRouterAt(Minecraft.getInstance().world, routerPos1));
            case "item_router":
                BlockPos routerPos2 = xtra.readBlockPos();
                return new GuiItemRouter(player.inventory, TileEntityItemRouter.getRouterAt(Minecraft.getInstance().world, routerPos2));
        }
        return null;
    }
}
