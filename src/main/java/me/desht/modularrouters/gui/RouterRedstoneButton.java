package me.desht.modularrouters.gui;

import me.desht.modularrouters.gui.widgets.TexturedCyclerButton;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class RouterRedstoneButton extends TexturedCyclerButton<RouterRedstoneBehaviour> {
    public RouterRedstoneButton(int buttonId, int x, int y, int width, int height, ResourceLocation location, RouterRedstoneBehaviour initialVal) {
        super(buttonId, x, y, width, height, location, initialVal);
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
    public List<String> getTooltip() {
        return Collections.singletonList(I18n.format("guiText.tooltip.redstone." + getState().name()));
    }
}
