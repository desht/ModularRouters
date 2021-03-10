package me.desht.modularrouters.client.gui.widgets.button;

import net.minecraft.client.gui.widget.button.Button.IPressable;

public class BackButton extends TexturedButton {
    public BackButton(int x, int y, IPressable pressable) {
        super(x, y, 16, 16, pressable);
    }

    @Override
    protected int getTextureX() {
        return 96;
    }

    @Override
    protected int getTextureY() {
        return 0;
    }

    @Override
    protected boolean drawStandardBackground() {
        return false;
    }
}
