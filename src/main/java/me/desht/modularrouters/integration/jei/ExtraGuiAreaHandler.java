package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.client.gui.GuiItemRouter;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rectangle2d;

import java.util.Collection;
import java.util.Collections;

public class ExtraGuiAreaHandler implements IGlobalGuiHandler {
    @Override
    public Collection<Rectangle2d> getGuiExtraAreas() {
        if (Minecraft.getInstance().screen instanceof GuiItemRouter) {
            return ((GuiItemRouter) Minecraft.getInstance().screen).getExtraArea();
        }
        return Collections.emptyList();
    }
}
