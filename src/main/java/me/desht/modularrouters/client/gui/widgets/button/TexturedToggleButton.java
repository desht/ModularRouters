package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.gui.ISendToServer;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public abstract class TexturedToggleButton extends TexturedButton implements IToggleButton {
    protected final List<ITextComponent> tooltip2 = new ArrayList<>();
    private boolean toggled;

    public TexturedToggleButton(int x, int y, int width, int height, boolean toggled, ISendToServer dataSyncer) {
        super(x, y, width, height, button -> {
            ((TexturedToggleButton) button).toggle();
            if (dataSyncer != null) dataSyncer.sendToServer();
        });
        this.toggled = toggled;
    }

    public void toggle() {
        setToggled(!isToggled());
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public boolean isToggled() {
        return toggled;
    }

    @Override
    public List<ITextComponent> getTooltip() {
        return toggled ? tooltip2 : super.getTooltip();
    }
}
