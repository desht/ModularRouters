package me.desht.modularrouters.gui.widgets;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public abstract class GuiContainerBase extends GuiContainer {
    public GuiContainerBase(Container c) {
        super(c);
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        super.drawScreen(x, y, partialTicks);
        this.buttonList.stream().filter(button -> button.isMouseOver() && button instanceof TexturedButton).forEach(button -> {
            drawHoveringText(((TexturedButton) button).getTooltip(), x, y, fontRendererObj);
        });
    }

}
