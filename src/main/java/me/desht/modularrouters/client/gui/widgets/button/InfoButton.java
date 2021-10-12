package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;

public class InfoButton extends TexturedButton {
    private static final XYPoint TEXTURE_XY = new XYPoint(128, 0);

    public InfoButton(int x, int y, String key) {
        super(x, y, 16, 16, p -> {});
        MiscUtil.appendMultilineText(tooltip1, ChatFormatting.WHITE, "modularrouters.guiText.tooltip." + key);
    }

    @Override
    protected boolean drawStandardBackground() {
        return false;
    }

    @Override
    protected XYPoint getTextureXY() {
        return TEXTURE_XY;
    }
}
