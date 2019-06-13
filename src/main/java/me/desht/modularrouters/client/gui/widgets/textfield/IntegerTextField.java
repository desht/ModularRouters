package me.desht.modularrouters.client.gui.widgets.textfield;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Pattern;

public class IntegerTextField extends TextFieldWidgetMR {
    private final int min;
    private final int max;
    private int incr = 1;
    private int coarseIncr = 10;
    private int fineIncr = 1;

    private static final Pattern INT_MATCHER = Pattern.compile("^-?[0-9]+$");

    public IntegerTextField(TextFieldManager parent, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height, int min, int max) {
        super(parent, fontrendererObj, x, y, par5Width, par6Height);
        this.min = min;
        this.max = max;

        setMaxStringLength(Math.max(Integer.toString(min).length(), Integer.toString(max).length()));

        setValidator(input -> {
            if (input == null || input.isEmpty()) {
                return true;  // treat as numeric zero
            }
            if (!INT_MATCHER.matcher(input).matches()) {
                return false;
            }
            int n = Integer.parseInt(input);
            return n >= this.min && n <= this.max;
        });
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP:
                return adjustField(getAdjustment());
            case GLFW.GLFW_KEY_DOWN:
                return adjustField(-getAdjustment());
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
        adjustField(direction > 0 ? getAdjustment() : -getAdjustment());
    }

    public void setValue(int val) {
        if (val >= min && val <= max) {
            setText(Integer.toString(val));
        }
    }

    public int getValue() {
        int val;
        try {
            val = Integer.parseInt(getText());
        } catch (NumberFormatException e) {
            val = min;
        }
        return val;
    }

    public void setIncr(int incr, int coarseAdjustMult) {
        setIncr(incr, coarseAdjustMult, 1);
    }

    public void setIncr(int incr, int coarseAdjustMult, int fineAdjustDiv) {
        this.incr = incr;
        this.coarseIncr = incr * coarseAdjustMult;
        this.fineIncr = incr / fineAdjustDiv;
    }

    private int getAdjustment() {
        if (Screen.hasShiftDown()) {
            return coarseIncr;
        } else if (Screen.hasControlDown()) {
            return fineIncr;
        } else {
            return incr;
        }
    }

    private boolean adjustField(int adj) {
        int newVal = Math.max(min, Math.min(max, getValue() + adj));
        if (newVal != getValue()) {
            setText("");
            writeText(Integer.toString(newVal));
        }
        return true;
    }
}
