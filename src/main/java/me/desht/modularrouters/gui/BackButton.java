package me.desht.modularrouters.gui;

import me.desht.modularrouters.gui.widgets.button.TexturedButton;

public class BackButton extends TexturedButton {
    public BackButton(int buttonId, int x, int y) {
        super(buttonId, x, y, 16, 16);
    }

    @Override
    protected int getTextureX() {
        return 96;
    }

    @Override
    protected int getTextureY() {
        return 0;
    }

    @Override
    protected boolean drawStandardBackground() {
        return false;
    }
}
