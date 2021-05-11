package me.desht.modularrouters.client.util;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ClientUtil {
    public static World theClientWorld() {
        return Minecraft.getInstance().level;
    }

    public static TileEntityItemRouter getOpenItemRouter() {
        if (Minecraft.getInstance().screen instanceof GuiItemRouter) {
            return ((GuiItemRouter) Minecraft.getInstance().screen).getMenu().getRouter();
        } else {
            return null;
        }
    }

    public static boolean thisScreenPassesEvents() {
        return Minecraft.getInstance().screen == null || Minecraft.getInstance().screen.passEvents;
    }

    public static IFormattableTextComponent xlate(String key, Object... args) {
        // not using TranslationTextComponent here because each argument starts a separate child component,
        // which resets any formatting each time
        return new StringTextComponent(I18n.get(key, args));
    }

    public static boolean isInvKey(int keyCode) {
        return keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue();
    }
}
