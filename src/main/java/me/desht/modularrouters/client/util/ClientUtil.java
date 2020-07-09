package me.desht.modularrouters.client.util;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.client.gui.upgrade.GuiSyncUpgrade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ClientUtil {
    public static World theClientWorld() {
        return Minecraft.getInstance().world;
    }

    public static TileEntityItemRouter getOpenItemRouter() {
        if (Minecraft.getInstance().currentScreen instanceof GuiItemRouter) {
            return ((GuiItemRouter) Minecraft.getInstance().currentScreen).router;
        } else {
            return null;
        }
    }

    public static boolean thisScreenPassesEvents() {
        return Minecraft.getInstance().currentScreen == null || Minecraft.getInstance().currentScreen.passEvents;
    }

    public static PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static void openSyncGui(ItemStack stack, Hand hand) {
        Minecraft.getInstance().displayGuiScreen(new GuiSyncUpgrade(stack, hand));
    }

    public static IFormattableTextComponent xlate(String key, Object... args) {
        // not using TranslationTextComponent here because each argument starts a separate child component,
        // which resets any formatting each time
        return new StringTextComponent(I18n.format(key, args));
    }
}
