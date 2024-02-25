package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.gui.ISendToServer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public abstract class TexturedToggleButton extends TexturedButton implements IToggleButton {
    private boolean toggled;
    private Tooltip untoggledTooltip;
    private Tooltip toggledTooltip;

    public TexturedToggleButton(int x, int y, int width, int height, boolean toggled, ISendToServer dataSyncer) {
        super(x, y, width, height, button -> {
            ((TexturedToggleButton) button).toggle();
            if (dataSyncer != null) dataSyncer.sendToServer();
        });
        setToggled(toggled);
    }

    protected TexturedToggleButton setTooltips(Tooltip off, Tooltip on) {
        untoggledTooltip = off;
        toggledTooltip = on;
        setToggled(toggled);
        return this;
    }

    protected TexturedToggleButton setTooltips(Component c1, Component c2) {
        return setTooltips(Tooltip.create(c1), Tooltip.create(c2));
    }

    public void toggle() {
        setToggled(!isToggled());
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
        setTooltip(toggled ? toggledTooltip : untoggledTooltip);
    }

    public boolean isToggled() {
        return toggled;
    }
}
