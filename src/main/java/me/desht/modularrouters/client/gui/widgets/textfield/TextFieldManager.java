package me.desht.modularrouters.client.gui.widgets.textfield;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Handles a collection of TextFieldWidget objects, sending events and managing Tab-focus etc.
 */
public class TextFieldManager {
    private final List<TextFieldWidgetMR> textFields = Lists.newArrayList();
    private int focusedField = -1;
    private final Screen parent;

    public TextFieldManager(Screen parent) {
        this.parent = parent;
    }

    public void drawTextFields(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        textFields.forEach(tf -> tf.renderButton(mouseX, mouseY, partialTicks));
    }

    public void tick() {
        if (focusedField >= 0) {
            textFields.get(focusedField).tick();
        }
    }

    public boolean mouseClicked(double x, double y, int btn) {
        for (int i = 0; i < textFields.size(); i++) {
            if (textFields.get(i).mouseClicked(x, y, btn)) {
                focus(i);
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(double wheel) {
        if (wheel == 0) {
            return false;
        } else if (isFocused() && textFields.get(focusedField).getVisible()) {
            textFields.get(focusedField).onMouseWheel(wheel < 0 ? -1 : 1);
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         if (keyCode == GLFW.GLFW_KEY_TAB) {
             cycleFocus(Screen.hasShiftDown() ? -1 : 1);
        } else if (isFocused() && textFields.get(focusedField).getVisible()) {
            return textFields.get(focusedField).keyPressed(keyCode, scanCode, modifiers);
            // avoid closing window while text field focused
//            return keyCode == GLFW.GLFW_KEY_E;
        }
        return false;
    }


    public boolean charTyped(char c, int modifiers) {
        return isFocused() && textFields.get(focusedField).getVisible()
                && textFields.get(focusedField).charTyped(c, modifiers);
    }

    int addTextField(TextFieldWidgetMR textField) {
        textFields.add(textField);
        return textFields.size() - 1;
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

    private void cycleFocus(int dir) {
        int f = focusedField;
        int oldF = f;

        do {
            f += dir;
            if (f < 0) {
                f = textFields.size() - 1;
            } else if (f >= textFields.size()) {
                f = 0;
            }
        } while (!textFields.get(f).getVisible() && f != focusedField);

        if (f != oldF) {
            focus(f);
            textFields.get(f).setCursorPositionEnd();
            textFields.get(f).setSelectionPos(0);
            if (oldF >= 0 && oldF < textFields.size()) {
                textFields.get(oldF).setSelectionPos(textFields.get(oldF).getCursorPosition());
            }
        }
    }

    public boolean isFocused() {
        return focusedField >= 0;
    }

    void onTextFieldFocusChange(int ordinal, boolean newFocus) {
        TextFieldWidgetMR tf = textFields.get(ordinal);
        if (newFocus) {
            focus(ordinal);
            tf.setCursorPositionEnd();
            tf.setSelectionPos(0);
        } else {
            tf.setSelectionPos(tf.getCursorPosition());
        }
//        if (ordinal == textFields.size() - 1) {
//            focusedField = -1;
//            for (TextFieldWidgetMR t : textFields) {
//                if (t.isFocused()) {
//                    focusedField = t.getOrdinal();
//                    break;
//                }
//            }
//        }
    }

    public TextFieldManager clear() {
        textFields.clear();
        focusedField = -1;
        return this;
    }

}
