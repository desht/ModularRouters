package me.desht.modularrouters.client.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.client.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiScreenBase extends Screen {
    private TextFieldManager textFieldManager;
    private int delayTicks;

    protected GuiScreenBase(ITextComponent displayName) {
        super(displayName);
    }

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
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (textFieldManager != null) textFieldManager.drawTextFields(matrixStack, mouseX, mouseY, partialTicks);
        this.buttons.stream()
                .filter(button -> button.isMouseOver(mouseX, mouseY) && button instanceof ITooltipButton)
                .forEach(button -> renderTooltip(matrixStack, ((ITooltipButton) button).getTooltip(), mouseX, mouseY, font));
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
