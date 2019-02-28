package me.desht.modularrouters.client.gui.widgets;

import me.desht.modularrouters.client.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

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
    public void render(int x, int y, float partialTicks) {
        this.drawDefaultBackground();
        super.render(x, y, partialTicks);
        if (textFieldManager != null) {
            textFieldManager.drawTextFields(x, y, partialTicks);
        }
        this.buttons.stream()
                .filter(button -> button.isMouseOver() && button instanceof ITooltipButton)
                .forEach(button -> drawHoveringText(((ITooltipButton) button).getTooltip(), x, y, fontRenderer));

        this.renderHoveredToolTip(x, y);
    }

    @Override
    public void tick() {
        super.tick();
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

    public boolean isFocused() {
        return textFieldManager != null && textFieldManager.isFocused();
    }

    @Override
    public void resync(ItemStack stack) {
        // nothing by default
    }
}
