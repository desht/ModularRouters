package me.desht.modularrouters.client.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;

public class GuiUtil {
    public static void drawFrame(GuiGraphicsExtractor graphics, AbstractWidget widget, int color) {
        graphics.horizontalLine(widget.getX(), widget.getX() + widget.getWidth() - 1, widget.getY(), color);
        graphics.horizontalLine(widget.getX(), widget.getX() + widget.getWidth() - 1, widget.getY() + widget.getHeight() - 1, color);
        graphics.verticalLine(widget.getX(), widget.getY(), widget.getY() + widget.getHeight() - 1, color);
        graphics.verticalLine(widget.getX() + widget.getWidth() - 1, widget.getY(), widget.getY() + widget.getHeight() - 1, color);
    }
}
