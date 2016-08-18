package me.desht.modularrouters.gui;

import me.desht.modularrouters.gui.widgets.TexturedToggleButton;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.MiscUtil;

public class ModuleToggleButton extends TexturedToggleButton {

    public ModuleToggleButton(Module.FilterSettings setting, int x, int y) {
        super(setting.ordinal(), x, y, GuiModule.BUTTON_WIDTH, GuiModule.BUTTON_HEIGHT);
        MiscUtil.appendMultiline(tooltip1, "guiText.tooltip." + Module.FilterSettings.values()[id] + ".1");
        MiscUtil.appendMultiline(tooltip2, "guiText.tooltip." + Module.FilterSettings.values()[id] + ".2");
    }

    @Override
    protected int getTextureX() {
        return this.id * GuiModule.BUTTON_WIDTH * 2 + (isToggled() ? GuiModule.BUTTON_WIDTH : 0);
    }

    @Override
    protected int getTextureY() {
        return 32;
    }

}
