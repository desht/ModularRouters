package me.desht.modularrouters.client.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class GuiUtil {
    public static void drawFrame(GuiGraphics graphics, AbstractWidget widget, int color) {
        graphics.hLine(widget.getX(), widget.getX() + widget.getWidth() - 1, widget.getY(), color);
        graphics.hLine(widget.getX(), widget.getX() + widget.getWidth() - 1, widget.getY() + widget.getHeight() - 1, color);
        graphics.vLine(widget.getX(), widget.getY(), widget.getY() + widget.getHeight() - 1, color);
        graphics.vLine(widget.getX() + widget.getWidth() - 1, widget.getY(), widget.getY() + widget.getHeight() - 1, color);
    }

    public static List<FormattedCharSequence> wrapTextComponentList(List<Component> text, int maxWidth, Font font) {
        ImmutableList.Builder<FormattedCharSequence> builder = ImmutableList.builder();
        for (Component line : text) {
            builder.addAll(ComponentRenderUtils.wrapComponents(line, maxWidth, font));
        }
        return builder.build();
    }
}
