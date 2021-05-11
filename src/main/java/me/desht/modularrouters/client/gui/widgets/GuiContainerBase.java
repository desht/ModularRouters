package me.desht.modularrouters.client.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.client.gui.IResyncableGui;
import me.desht.modularrouters.client.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiContainerBase<T extends Container> extends ContainerScreen<T> implements IResyncableGui, IManagedTextFields {
    private TextFieldManager textFieldManager;

    public GuiContainerBase(T container, PlayerInventory inv, ITextComponent displayName) {
        super(container, inv, displayName);
    }

    @Override
    public TextFieldManager getOrCreateTextFieldManager() {
        if (textFieldManager == null) textFieldManager = new TextFieldManager(this);
        return textFieldManager;
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, x, y, partialTicks);
        if (textFieldManager != null) {
            textFieldManager.drawTextFields(matrixStack, x, y, partialTicks);
        }
        this.buttons.stream()
                .filter(button -> button.isMouseOver(x, y) && button instanceof ITooltipButton)
                .findFirst()
                .ifPresent(button -> renderComponentTooltip(matrixStack, ((ITooltipButton) button).getTooltip(), x, y));

        this.renderTooltip(matrixStack, x, y);
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
        if (keyCode == 256) {
            this.minecraft.player.closeContainer();
        }
        if (textFieldManager != null && (textFieldManager.keyPressed(keyCode, scanCode, modifiers) || textFieldManager.isFocused())) {
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
