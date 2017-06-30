package me.desht.modularrouters.gui.widgets.button;

import me.desht.modularrouters.util.MiscUtil;

public class InfoButton extends TexturedButton {
    public InfoButton(int buttonId, int x, int y, String key) {
        super(buttonId, x, y, 16, 16);
        MiscUtil.appendMultiline(tooltip1, "guiText.tooltip." + key);
    }

    @Override
    protected boolean drawStandardBackground() {
        return false;
    }

    @Override
    protected int getTextureX() {
        return 128;
    }

    @Override
    protected int getTextureY() {
        return 0;
    }
}
