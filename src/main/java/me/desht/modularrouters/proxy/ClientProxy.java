package me.desht.modularrouters.proxy;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.client.gui.upgrade.GuiSyncUpgrade;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ClientProxy implements IProxy {
    @Override
    public World theClientWorld() {
        return Minecraft.getInstance().world;
    }

    @Override
    public TileEntityItemRouter getOpenItemRouter() {
        if (Minecraft.getInstance().currentScreen instanceof GuiItemRouter) {
            return ((GuiItemRouter) Minecraft.getInstance().currentScreen).router;
        } else {
            return null;
        }
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void openSyncGui(ItemStack stack, Hand hand) {
        Minecraft.getInstance().displayGuiScreen(new GuiSyncUpgrade(stack, hand));
    }
}
