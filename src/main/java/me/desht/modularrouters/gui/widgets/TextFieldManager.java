package me.desht.modularrouters.gui.widgets;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public class TextFieldManager {
    private final List<TextFieldWidget> textFields = Lists.newArrayList();
    private int focusedField = -1;
    private final GuiScreen parent;

    public TextFieldManager(GuiScreen parent) {
        this.parent = parent;
    }

    void drawTextFields() {
        textFields.forEach(TextFieldWidget::drawTextBox);
    }

    void updateTextFields() {
        if (focusedField >= 0) {
            textFields.get(focusedField).updateCursorCounter();
        }
    }

    /**
     * See if any textfields are interested in a mouse event
     *
     * @return true if a text field handle the mouse event, false if not
     * @throws IOException
     */
    boolean handleMouseInput() throws IOException {
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return false;
        } else if (focusedField >= 0) {
            textFields.get(focusedField).onMouseWheel(wheel < 0 ? -1 : 1);
            return true;
        } else {
            // check if mouse is over an unfocused field, if so focus on it
            int mouseX = Mouse.getEventX() * parent.width / parent.mc.displayWidth;
            int mouseY = parent.height - Mouse.getEventY() * parent.height / parent.mc.displayHeight - 1;
            for (int i = 0; i < textFields.size(); i++) {
                TextFieldWidget field = textFields.get(i);
                if (mouseX >= field.xPosition && mouseX < field.xPosition + field.width && mouseY >= field.yPosition && mouseY < field.yPosition + field.height) {
                    focus(i);
                    field.onMouseWheel(wheel < 0 ? -1 : 1);
                    return true;
                }
            }
        }
        return false;
    }

    void mouseClicked(int x, int y, int btn) throws IOException {
        for (TextFieldWidget field : textFields) {
            field.mouseClicked(x, y, btn);
        }
    }

    boolean keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_TAB) {
            if (GuiScreen.isShiftKeyDown()) {
                focusPrev();
            } else {
                focusNext();
            }
        } else if (isFocused()) {
            textFields.get(focusedField).textboxKeyTyped(typedChar, keyCode);
            if (keyCode == Keyboard.KEY_E) return true;  // avoid closing window while text field focused
        }
        return false;
    }

    public void addTextField(TextFieldWidget textField) {
        textFields.add(textField);
        textField.setOrdinal(textFields.size() - 1);
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

    private void focusNext() {
        int field = focusedField + 1;
        if (field >= textFields.size()) field = 0;
        focus(field);
    }

    private void focusPrev() {
        int field = focusedField - 1;
        if (field < 0) field = textFields.size() - 1;
        focus(field);
    }

    public boolean isFocused() {
        return focusedField >= 0;
    }

    void onTextFieldFocusChange(int ordinal, boolean newFocus) {
        if (ordinal == textFields.size() - 1) {
            focusedField = -1;
            for (TextFieldWidget t : textFields) {
                if (t.isFocused()) {
                    focusedField = t.getOrdinal();
                    break;
                }
            }
        }
    }

    public TextFieldManager clear() {
        textFields.clear();
        focusedField = -1;
        return this;
    }
}
