package me.desht.modularrouters.client.gui.widgets;

import me.desht.modularrouters.client.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiContainerBase<T extends Container> extends ContainerScreen<T> implements IResyncableGui {
    private TextFieldManager textFieldManager;

    public GuiContainerBase(T container, PlayerInventory inv, ITextComponent displayName) {
        super(container, inv, displayName);
    }

    protected TextFieldManager createTextFieldManager() {
        textFieldManager = new TextFieldManager(this);
        return textFieldManager;
    }

    protected TextFieldManager getOrCreateTextFieldManager() {
        if (textFieldManager == null) textFieldManager = createTextFieldManager();

        return textFieldManager;
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        this.renderBackground();
        super.render(x, y, partialTicks);
        if (textFieldManager != null) {
            textFieldManager.drawTextFields(x, y, partialTicks);
        }
        this.buttons.stream()
                .filter(button -> button.isMouseOver(x, y) && button instanceof ITooltipButton)
                .forEach(button -> renderTooltip(((ITooltipButton) button).getTooltip(), x, y, font));

        this.renderHoveredToolTip(x, y);
    }

    @Override
    public void tick() {
        super.tick();
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

    protected boolean isFocused() {
        return textFieldManager != null && textFieldManager.isFocused();
    }

    @Override
    public void resync(ItemStack stack) {
        // nothing by default
    }
}
