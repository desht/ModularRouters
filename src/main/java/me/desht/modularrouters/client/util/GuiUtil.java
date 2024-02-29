package me.desht.modularrouters.client.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

public class GuiUtil {
    public static void drawFrame(GuiGraphics graphics, AbstractWidget widget, int color) {
        graphics.hLine(widget.getX(), widget.getX() + widget.getWidth() - 1, widget.getY(), color);
        graphics.hLine(widget.getX(), widget.getX() + widget.getWidth() - 1, widget.getY() + widget.getHeight() - 1, color);
        graphics.vLine(widget.getX(), widget.getY(), widget.getY() + widget.getHeight() - 1, color);
        graphics.vLine(widget.getX() + widget.getWidth() - 1, widget.getY(), widget.getY() + widget.getHeight() - 1, color);
    }
}
