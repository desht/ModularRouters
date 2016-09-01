package me.desht.modularrouters.gui.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class TextFieldWidget extends GuiTextField {
    private final GuiContainerBase parent;

    public TextFieldWidget(GuiContainerBase parent, int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
        this.parent = parent;
    }

    @Override
    public void setFocused(boolean isFocusedIn) {
        super.setFocused(isFocusedIn);
        parent.onTextFieldFocusChange(getId(), isFocusedIn);
        setTextColor(isFocusedIn ? 0xffffff : 0xc0c0c0);
    }

    public void onMouseWheel(int direction) {}
}
