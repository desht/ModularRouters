package me.desht.modularrouters.gui.widgets;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public abstract class GuiContainerBase extends GuiContainer implements IResyncableGui {
    private TextFieldManager textFieldManager;

    public GuiContainerBase(Container container) {
        super(container);
    }

    protected TextFieldManager getTextFieldManager() {
        if (textFieldManager == null) {
            textFieldManager = new TextFieldManager(this);
        }
        return textFieldManager;
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        super.drawScreen(x, y, partialTicks);
        if (textFieldManager != null) textFieldManager.drawTextFields();
        this.buttonList.stream()
                .filter(button -> button.isMouseOver() && button instanceof ITooltipButton)
                .forEach(button -> drawHoveringText(((ITooltipButton) button).getTooltip(), x, y, fontRendererObj));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (textFieldManager != null) textFieldManager.updateTextFields();
    }

    @Override
    public void handleMouseInput() throws IOException {
        if (textFieldManager == null || !textFieldManager.handleMouseInput()) {
            super.handleMouseInput();
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) throws IOException {
        super.mouseClicked(x, y, btn);
        if (textFieldManager != null) textFieldManager.mouseClicked(x, y, btn);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (textFieldManager == null || !textFieldManager.keyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    public boolean isFocused() {
        return textFieldManager != null && textFieldManager.isFocused();
    }

    @Override
    public void resync(ItemStack stack) {
        // nothing by default
    }
}
