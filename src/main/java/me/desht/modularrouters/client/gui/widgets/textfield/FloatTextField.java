package me.desht.modularrouters.client.gui.widgets.textfield;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public class FloatTextField extends TextFieldWidgetMR {
    private final float min;
    private final float max;
    private float incr = 1.0f;
    private float fine = 0.1f;
    private float coarse = 5.0f;
    private String precStr = "%.1f";

    public FloatTextField(TextFieldManager parent, Font fontrendererObj, int x, int y, int par5Width, int par6Height, float min, float max) {
        super(parent, fontrendererObj, x, y, par5Width, par6Height);
        this.min = min;
        this.max = max;

        setMaxLength(5);
        setFilter(input -> {
            if (input == null || input.isEmpty() || input.equals("-")) {
                return true;  // treat as numeric zero
            }
            try {
                float f = Float.parseFloat(input);
                return f >= this.min && f <= this.max;
            } catch (NumberFormatException e) {
                return false;
            }
        });
    }

    public void setIncr(float incr, float fine, float coarse) {
        this.incr = incr;
        this.fine = fine;
        this.coarse = coarse;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP:
                return adjustField(incr);
            case GLFW.GLFW_KEY_DOWN:
                return adjustField(-incr);
            case GLFW.GLFW_KEY_PAGE_UP:
                return adjustField(max);
            case GLFW.GLFW_KEY_PAGE_DOWN:
                return adjustField(-max);
            default:
                return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void onMouseWheel(int direction) {
        adjustField(direction > 0 ? incr : -incr);
    }

    public void setValue(float newVal) {
        if (newVal >= min && newVal <= max) {
            setValue(String.format(precStr, newVal));
        }
    }

    public void setPrecision(int precision) {
        precStr = "%." + precision + "f";
    }

    private boolean adjustField(float adj) {
        if (Screen.hasControlDown()) {
            adj *= fine;
        } else if (Screen.hasShiftDown()) {
            adj *= coarse;
        }
        float val;
        try {
            val = Float.parseFloat(getValue());
        } catch (NumberFormatException e) {
            val = min;
        }
        float newVal = Math.max(min, Math.min(max, val + adj));
        if (newVal != val) {
            setValue("");
            insertText(String.format(precStr, newVal));
        }
        return true;
    }

    public float getFloatValue() {
        try {
            return Float.parseFloat(getValue());
        } catch (NumberFormatException e) {
            return 0f;
        }
    }
}
