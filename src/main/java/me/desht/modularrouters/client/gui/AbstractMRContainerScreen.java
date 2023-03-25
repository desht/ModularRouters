package me.desht.modularrouters.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.client.gui.widgets.IManagedTextFields;
import me.desht.modularrouters.client.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractMRContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IResyncableGui, IManagedTextFields {
    private TextFieldManager textFieldManager;

    public AbstractMRContainerScreen(T container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public TextFieldManager getOrCreateTextFieldManager() {
        if (textFieldManager == null) textFieldManager = new TextFieldManager();
        return textFieldManager;
    }

    @Override
    public void render(PoseStack matrixStack, int x, int y, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, x, y, partialTicks);
        if (textFieldManager != null) {
            textFieldManager.drawTextFields(matrixStack, x, y, partialTicks);
        }
        this.renderables.stream()
                .filter(widget -> widget instanceof AbstractWidget aw && aw.isMouseOver(x, y) && widget instanceof ITooltipButton)
                .findFirst()
                .ifPresent(button -> renderComponentTooltip(matrixStack, ((ITooltipButton) button).getTooltip(), x, y));

        this.renderTooltip(matrixStack, x, y);
    }

    @Override
    public void containerTick() {
        super.containerTick();
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

    @Override
    public boolean isFocused() {
        return textFieldManager != null && textFieldManager.isFocused();
    }

    @Override
    public void resync(ItemStack stack) {
        // nothing by default
    }
}
