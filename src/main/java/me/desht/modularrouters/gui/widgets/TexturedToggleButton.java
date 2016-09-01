package me.desht.modularrouters.gui.widgets;

import java.util.ArrayList;
import java.util.List;

public abstract class TexturedToggleButton extends TexturedButton implements ToggleButton {
    protected final List<String> tooltip2 = new ArrayList<>();
    private boolean toggled;

    public TexturedToggleButton(int buttonId, int x, int y, int width, int height) {
        super(buttonId, x, y, width, height);
    }

    public void toggle() {
        toggled = !toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public boolean isToggled() {
        return toggled;
    }

    @Override
    public List<String> getTooltip() {
        return toggled ? tooltip2 : super.getTooltip();
    }

}
