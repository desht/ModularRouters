package me.desht.modularrouters.gui.widgets;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public abstract class GuiContainerBase extends GuiContainer {
    public GuiContainerBase(Container c) {
        super(c);
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        super.drawScreen(x, y, partialTicks);
        for (GuiButton button : this.buttonList) {
            if (button.isMouseOver() && button instanceof TexturedButton) {
                drawHoveringText(((TexturedButton) button).getTooltip(), x, y, fontRendererObj);
            }
        }
    }

}
