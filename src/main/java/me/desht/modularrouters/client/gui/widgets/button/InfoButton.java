package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.util.text.TextFormatting;

public class InfoButton extends TexturedButton {
    public InfoButton(int x, int y, String key) {
        super(x, y, 16, 16, p -> {});
        MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, "guiText.tooltip." + key);
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
