package me.desht.modularrouters.client.gui.widgets;

import me.desht.modularrouters.client.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public abstract class GuiScreenBase extends GuiScreen {
    private TextFieldManager textFieldManager;
    private int delayTicks;

    protected TextFieldManager getTextFieldManager() {
        if (textFieldManager == null) {
            textFieldManager = new TextFieldManager(this);
        }
        return textFieldManager;
    }

    protected boolean hasTextFieldManager() {
        return textFieldManager != null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (textFieldManager != null) textFieldManager.drawTextFields();
        this.buttonList.stream()
                .filter(button -> button.isMouseOver() && button instanceof ITooltipButton)
                .forEach(button -> drawHoveringText(((ITooltipButton) button).getTooltip(), mouseX, mouseY, fontRenderer));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (delayTicks > 0) {
            delayTicks--;
            if (delayTicks == 0) {
                sendSettingsToServer();
            }
        }
        if (textFieldManager != null) textFieldManager.updateTextFields();
    }

    @Override
    public void handleMouseInput() throws IOException {
        if (textFieldManager == null || !textFieldManager.handleMouseInput()) {
            super.handleMouseInput();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (textFieldManager != null) textFieldManager.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (textFieldManager == null || !textFieldManager.keyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void onGuiClosed() {
        if (delayTicks > 0) {
            // flush pending changes
            sendSettingsToServer();
        }
        super.onGuiClosed();
    }

    protected final void sendSettingsDelayed(int delayTicks) {
        this.delayTicks = delayTicks;
    }

    protected void sendSettingsToServer() {
        // does nothing, override in subclasses
    }
}
