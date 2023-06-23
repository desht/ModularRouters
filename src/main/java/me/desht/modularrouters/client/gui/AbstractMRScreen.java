package me.desht.modularrouters.client.gui;

import me.desht.modularrouters.client.gui.widgets.IManagedTextFields;
import me.desht.modularrouters.client.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractMRScreen extends Screen implements IManagedTextFields {
    private TextFieldManager textFieldManager;
    private int delayTicks;

    protected AbstractMRScreen(Component displayName) {
        super(displayName);
    }

    @Override
    public TextFieldManager getOrCreateTextFieldManager() {
        if (textFieldManager == null) {
            textFieldManager = new TextFieldManager();
        }
        return textFieldManager;
    }

    protected boolean hasTextFieldManager() {
        return textFieldManager != null;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (textFieldManager != null) textFieldManager.drawTextFields(graphics, mouseX, mouseY, partialTicks);
        this.renderables.stream()
                .filter(widget -> widget instanceof AbstractWidget aw && aw.isMouseOver(mouseX, mouseY) && widget instanceof ITooltipButton)
                .findFirst()
                .ifPresent(button -> graphics.renderComponentTooltip(font, ((ITooltipButton) button).getTooltipLines(), mouseX, mouseY));
    }

    @Override
    public void tick() {
        if (delayTicks > 0) {
            delayTicks--;
            if (delayTicks == 0) {
                sendSettingsToServer();
            }
        }
        if (textFieldManager != null) textFieldManager.tick();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dir) {
        return textFieldManager != null ? textFieldManager.mouseScrolled(dir) : super.mouseScrolled(x, y, dir);
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
    public void removed() {
        if (delayTicks > 0) {
            // flush pending changes
            sendSettingsToServer();
        }
        super.removed();
    }

    protected final void sendSettingsDelayed(int delayTicks) {
        this.delayTicks = delayTicks;
    }

    protected void sendSettingsToServer() {
        // does nothing, override in subclasses
    }
}
