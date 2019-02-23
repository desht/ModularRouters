package me.desht.modularrouters.client.gui.widgets;

import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
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
        if (textFieldManager != null) textFieldManager.drawTextFields(x, y, partialTicks);
        // @todo 1.13
//        this.buttonList.stream()
//                .filter(button -> button.isMouseOver() && button instanceof ITooltipButton)
//                .forEach(button -> drawHoveringText(((ITooltipButton) button).getTooltip(), x, y, fontRenderer));
        this.renderHoveredToolTip(x, y);
    }

    @Override
    public void tick() {
        super.tick();
        if (textFieldManager != null) textFieldManager.tick();
    }

    // todo 1.13
//    @Override
//    public void handleMouseInput() throws IOException {
//        if (textFieldManager == null || !textFieldManager.handleMouseInput()) {
//            super.handleMouseInput();
//        }
//    }
//
//    @Override
//    protected void mouseClicked(int x, int y, int btn) throws IOException {
//        super.mouseClicked(x, y, btn);
//        if (textFieldManager != null) textFieldManager.mouseClicked(x, y, btn);
//    }
//
//    @Override
//    protected void keyTyped(char typedChar, int keyCode) throws IOException {
//        if (textFieldManager == null || !textFieldManager.keyTyped(typedChar, keyCode)) {
//            super.keyTyped(typedChar, keyCode);
//        }
//    }

    public boolean isFocused() {
        return textFieldManager != null && textFieldManager.isFocused();
    }

    @Override
    public void resync(ItemStack stack) {
        // nothing by default
    }
}
