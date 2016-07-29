package me.desht.modularrouters.gui;

import me.desht.modularrouters.gui.widgets.TexturedToggleButton;
import me.desht.modularrouters.item.module.AbstractModule;
import me.desht.modularrouters.util.MiscUtil;

public class ModuleToggleButton extends TexturedToggleButton {
    private static final int BUTTON_WIDTH = 16;
    private static final int BUTTON_HEIGHT = 16;

    public ModuleToggleButton(int buttonId, int x, int y) {
        super(buttonId, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        MiscUtil.processTooltip(tooltip1, "guiText.tooltip." + AbstractModule.FilterSettings.values()[buttonId] + ".1");
        MiscUtil.processTooltip(tooltip2, "guiText.tooltip." + AbstractModule.FilterSettings.values()[buttonId] + ".2");
    }

    @Override
    protected int getTextureX() {
        return this.id * BUTTON_WIDTH * 2 + (isToggled() ? BUTTON_WIDTH : 0);
    }

    @Override
    protected int getTextureY() {
        return 32;
    }

}
