package me.desht.modularrouters.gui;

import me.desht.modularrouters.gui.widgets.RadioButton;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class DirectionButton extends RadioButton {
    private static final int DIRECTION_GROUP = 1;
    private final Module.RelativeDirection direction;

    public DirectionButton(Module.RelativeDirection dir, int x, int y) {
        super(dir.ordinal() + GuiModule.DIRECTION_BASE_ID, DIRECTION_GROUP, x, y, GuiModule.BUTTON_WIDTH, GuiModule.BUTTON_HEIGHT);
        this.direction = dir;
        tooltip1.add(TextFormatting.GRAY + I18n.format("guiText.tooltip." + dir));
        tooltip2.add(TextFormatting.YELLOW + I18n.format("guiText.tooltip." + dir));
    }

    @Override
    protected int getTextureX() {
        return direction.ordinal() * GuiModule.BUTTON_WIDTH * 2 + (isToggled() ? GuiModule.BUTTON_WIDTH : 0);
    }

    @Override
    protected int getTextureY() {
        return 48;
    }

    public Module.RelativeDirection getDirection() {
        return direction;
    }
}
