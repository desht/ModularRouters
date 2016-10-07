package me.desht.modularrouters.gui;

import me.desht.modularrouters.gui.widgets.TexturedCyclerButton;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.client.resources.I18n;

import java.util.Collections;

public class RedstoneBehaviourButton extends TexturedCyclerButton<RouterRedstoneBehaviour> {
    RedstoneBehaviourButton(int buttonId, int x, int y, int width, int height, RouterRedstoneBehaviour initialVal) {
        super(buttonId, x, y, width, height, initialVal);
    }

    @Override
    protected int getTextureX() {
        return 16 * getState().ordinal();
    }

    @Override
    protected int getTextureY() {
        return 16;
    }

    @Override
    public java.util.List<String> getTooltip() {
        return Collections.singletonList(I18n.format("guiText.tooltip.redstone.label") + ": " + I18n.format("guiText.tooltip.redstone." + getState().name()));
    }
}
