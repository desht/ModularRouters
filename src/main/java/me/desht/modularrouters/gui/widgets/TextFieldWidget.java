package me.desht.modularrouters.gui.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class TextFieldWidget extends GuiTextField {
    private final TextFieldManager manager;
    private int textColor = 0xe0e0e0;
    private int disabledTextColor = 0x707070;
    private int ordinal;  // order in which this field appears in the textfield manager

    public TextFieldWidget(TextFieldManager manager, int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
        this.manager = manager;
        manager.addTextField(this);
    }

    @Override
    public void setFocused(boolean isFocusedIn) {
        super.setFocused(isFocusedIn);
        manager.onTextFieldFocusChange(ordinal, isFocusedIn);
        setTextColor(isFocusedIn ? textColor : disabledTextColor);
    }

    @Override
    public void setTextColor(int color) {
        this.textColor = color;
        super.setTextColor(color);
    }

    @Override
    public void setDisabledTextColour(int color) {
        this.disabledTextColor = color;
        super.setDisabledTextColour(color);
    }

    @Override
    public void drawTextBox() {
        if (getVisible()) {
            if (!getEnableBackgroundDrawing()) {
                drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, 0x202020);
                drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 0xa0a0a0);
            }
            super.drawTextBox();
        }
    }

    public void onMouseWheel(int direction) {}

    /**
     * Get the order in which this field appears in the text field manager.
     *
     * @return the order
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * Should only be called by the text field manager when the field is added to it!
     *
     * @param ordinal
     */
    void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
}
