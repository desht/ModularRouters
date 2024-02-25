package me.desht.modularrouters.client.gui.widgets.textfield;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class TextFieldWidgetMR extends EditBox {
    public TextFieldWidgetMR(Font fontrendererObj, int x, int y, int width, int height) {
        super(fontrendererObj, x, y, width, height, Component.empty());
    }

    /**
     * Convenience method
     */
    public void useGuiTextBackground() {
        setTextColor(0xffffffff);
        setTextColorUneditable(0xffffffff);
        setBordered(false);
    }
}
