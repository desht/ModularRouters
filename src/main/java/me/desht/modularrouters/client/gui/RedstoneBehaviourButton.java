package me.desht.modularrouters.client.gui;

import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.client.resources.I18n;

import java.util.Collections;

public class RedstoneBehaviourButton extends TexturedCyclerButton<RouterRedstoneBehaviour> {
    public RedstoneBehaviourButton(int x, int y, int width, int height, RouterRedstoneBehaviour initialVal, ISendToServer dataSyncer) {
        super(x, y, width, height, initialVal, dataSyncer);
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
