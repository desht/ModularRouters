package me.desht.modularrouters.gui.widgets;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public abstract class GuiContainerBase extends GuiContainer {
    private final List<TextFieldWidget> textFields = Lists.newArrayList();
    private int focusedField = -1;

    public GuiContainerBase(Container c) {
        super(c);
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        super.drawScreen(x, y, partialTicks);
        textFields.forEach(TextFieldWidget::drawTextBox);
        this.buttonList.stream().filter(button -> button.isMouseOver() && button instanceof ITooltipButton).forEach(button -> {
            drawHoveringText(((ITooltipButton) button).getTooltip(), x, y, fontRendererObj);
        });
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (focusedField >= 0) {
            textFields.get(focusedField).updateCursorCounter();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            super.handleMouseInput();
        } else if (focusedField >= 0) {
            textFields.get(focusedField).onMouseWheel(wheel < 0 ? -1 : 1);
        } else {
            // check if mouse is over an unfocused field, if so focus on it
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            for (int i = 0; i < textFields.size(); i++) {
                TextFieldWidget field = textFields.get(i);
                if (mouseX >= field.xPosition && mouseX < field.xPosition + field.width && mouseY >= field.yPosition && mouseY < field.yPosition + field.height) {
                    focus(i);
                    field.onMouseWheel(wheel < 0 ? -1 : 1);
                    return;
                }
            }
            super.handleMouseInput();
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) throws IOException {
        super.mouseClicked(x, y, btn);
        for (TextFieldWidget field : textFields) {
            field.mouseClicked(x, y, btn);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_TAB) {
            if (GuiScreen.isShiftKeyDown()) {
                focusPrev();
            } else {
                focusNext();
            }
        } else if (isFocused()) {
            textFields.get(focusedField).textboxKeyTyped(typedChar, keyCode);
            if (keyCode == Keyboard.KEY_E) return;  // avoid closing window while text field focused
        }
        super.keyTyped(typedChar, keyCode);
    }

    public void addTextField(TextFieldWidget textField) {
        textFields.add(textField);
    }

    public void focus(int field) {
        if (field != focusedField) {
            if (focusedField != -1) {
                textFields.get(focusedField).setFocused(false);
            }
            focusedField = field;
            if (focusedField != -1) {
                textFields.get(focusedField).setFocused(true);
            }
        }
    }

    public void focusNext() {
        int field = focusedField + 1;
        if (field >= textFields.size()) field = 0;
        focus(field);
    }

    public void focusPrev() {
        int field = focusedField - 1;
        if (field < 0) field = textFields.size() - 1;
        focus(field);
    }

    protected boolean isFocused() {
        return focusedField >= 0;
    }

    protected void onTextFieldFocusChange(int id, boolean newFocus) {
        if (id == textFields.size() - 1) {
            focusedField = -1;
            for (TextFieldWidget t : textFields) {
                if (t.isFocused()) {
                    focusedField = t.getId();
                    break;
                }
            }
        }
    }
}
