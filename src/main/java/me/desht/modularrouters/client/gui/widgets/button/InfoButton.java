package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class InfoButton extends TexturedButton {
    private static final XYPoint TEXTURE_XY = new XYPoint(128, 0);

    public InfoButton(int x, int y, String key) {
        super(x, y, 16, 16, p -> {});
        setTooltip(Tooltip.create(xlate("modularrouters.guiText.tooltip." + key)));
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
