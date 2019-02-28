package me.desht.modularrouters.client.gui.widgets;

import me.desht.modularrouters.client.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import net.minecraft.client.gui.GuiScreen;

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
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        if (textFieldManager != null) textFieldManager.drawTextFields(mouseX, mouseY, partialTicks);
        this.buttons.stream()
                .filter(button -> button.isMouseOver() && button instanceof ITooltipButton)
                .forEach(button -> drawHoveringText(((ITooltipButton) button).getTooltip(), mouseX, mouseY, fontRenderer));
    }

    @Override
    public void tick() {
        super.tick();
        if (delayTicks > 0) {
            delayTicks--;
            if (delayTicks == 0) {
                sendSettingsToServer();
            }
        }
        if (textFieldManager != null) textFieldManager.tick();
    }

    @Override
    public boolean mouseScrolled(double dir) {
        return textFieldManager != null ?
                textFieldManager.mouseScrolled(dir) :
                super.mouseScrolled(dir);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (textFieldManager != null && textFieldManager.mouseClicked(x, y, button)) {
            return true;
        } else {
            return super.mouseClicked(x, y, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (textFieldManager != null && textFieldManager.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (textFieldManager != null && textFieldManager.charTyped(c, modifiers)) {
            return true;
        } else {
            return super.charTyped(c, modifiers);
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
