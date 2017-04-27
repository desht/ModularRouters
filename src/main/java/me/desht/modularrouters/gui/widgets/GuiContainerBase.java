package me.desht.modularrouters.gui.widgets;

import me.desht.modularrouters.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.gui.widgets.textfield.TextFieldManager;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public abstract class GuiContainerBase extends GuiContainer implements IResyncableGui {
    private TextFieldManager textFieldManager;

    public GuiContainerBase(Container container) {
        super(container);
    }

    protected boolean hasTextFieldManager() {
        return textFieldManager != null;
    }

    protected TextFieldManager createTextFieldManager() {
        textFieldManager = new TextFieldManager(this);
        return textFieldManager;
    }

    protected TextFieldManager getTextFieldManager() {
        return textFieldManager;
    }

    protected TextFieldManager getOrCreateTextFieldManager() {
        return hasTextFieldManager() ? getTextFieldManager() : createTextFieldManager();
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        super.drawScreen(x, y, partialTicks);
        if (textFieldManager != null) textFieldManager.drawTextFields();
        this.buttonList.stream()
                .filter(button -> button.isMouseOver() && button instanceof ITooltipButton)
                .forEach(button -> drawHoveringText(((ITooltipButton) button).getTooltip(), x, y, fontRenderer));
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
