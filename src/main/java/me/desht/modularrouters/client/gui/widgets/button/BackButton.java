package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.util.XYPoint;

public class BackButton extends TexturedButton {
    private static final XYPoint TEXTURE_XY = new XYPoint(96, 0);

    public BackButton(int x, int y, OnPress pressable) {
        super(x, y, 16, 16, pressable);
    }

    @Override
    protected XYPoint getTextureXY() {
        return TEXTURE_XY;
    }

    @Override
    protected boolean drawStandardBackground() {
        return false;
    }
}
