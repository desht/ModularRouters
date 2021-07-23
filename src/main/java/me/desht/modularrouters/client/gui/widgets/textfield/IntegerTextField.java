package me.desht.modularrouters.client.gui.widgets.textfield;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Range;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Pattern;

public class IntegerTextField extends TextFieldWidgetMR {
    private Range<Integer> range;
    private int incr = 1;
    private int coarseIncr = 10;
    private int fineIncr = 1;

    private static final Pattern INT_MATCHER = Pattern.compile("^-?[0-9]+$");

    public IntegerTextField(TextFieldManager parent, Font fontrendererObj, int x, int y, int par5Width, int par6Height, Range<Integer> range) {
        super(parent, fontrendererObj, x, y, par5Width, par6Height);

        setRange(range);

        setFilter(this::validate);
    }

    private boolean validate(String input) {
        if (input == null || input.isEmpty()) {
            return true;  // treat as numeric zero
        }
        if (!INT_MATCHER.matcher(input).matches()) {
            return false;
        }
        int n = Integer.parseInt(input);
        return range.contains(n);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP:
                return adjustField(getAdjustment());
            case GLFW.GLFW_KEY_DOWN:
                return adjustField(-getAdjustment());
            case GLFW.GLFW_KEY_PAGE_UP:
                return adjustField(range.getMaximum());
            case GLFW.GLFW_KEY_PAGE_DOWN:
                return adjustField(-range.getMaximum());
            default:
                return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void setRange(Range<Integer> range) {
        this.range = range;
        if (!range.contains(getIntValue())) {
            setValue(Mth.clamp(getIntValue(), range.getMinimum(), range.getMaximum()));
//            setCursorPosition(1);
        }
        setMaxLength(Math.max(Integer.toString(range.getMinimum()).length(), Integer.toString(range.getMaximum()).length()));
    }

    @Override
    public void onMouseWheel(int direction) {
        adjustField(direction > 0 ? getAdjustment() : -getAdjustment());
    }

    public void setValue(int val) {
        if (range.contains(val)) {
            setValue(Integer.toString(val));
        }
    }

    public int getIntValue() {
        int val;
        try {
            val = Integer.parseInt(getValue());
        } catch (NumberFormatException e) {
            val = range.getMinimum();
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
        int newVal = Mth.clamp(getIntValue() + adj, range.getMinimum(), range.getMaximum());
        if (newVal != getIntValue()) {
            setValue("");
            insertText(Integer.toString(newVal));
        }
        return true;
    }
}
