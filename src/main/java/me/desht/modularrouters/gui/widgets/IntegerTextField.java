package me.desht.modularrouters.gui.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.util.regex.Pattern;

public class IntegerTextField extends TextFieldWidget {
    private final int min;
    private final int max;
    private int incr = 1;
    private int coarseIncr = 10;
    private int fineIncr = 1;

    private static final Pattern INT_MATCHER = Pattern.compile("^-?[0-9]+$");

    public IntegerTextField(TextFieldManager parent, int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height, int min, int max) {
        super(parent, componentId, fontrendererObj, x, y, par5Width, par6Height);
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
    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        switch (keyCode) {
            case Keyboard.KEY_UP:
                return adjustField(getAdjustment());
            case Keyboard.KEY_DOWN:
                return adjustField(-getAdjustment());
            case Keyboard.KEY_PRIOR:
                return adjustField(max);
            case Keyboard.KEY_NEXT:
                return adjustField(-max);
            default:
                return super.textboxKeyTyped(typedChar, keyCode);
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

    public void setIncr(int incr, int coarseAdjustMult) {
        setIncr(incr, coarseAdjustMult, 1);
    }

    public void setIncr(int incr, int coarseAdjustMult, int fineAdjustDiv) {
        this.incr = incr;
        this.coarseIncr = incr * coarseAdjustMult;
        this.fineIncr = incr / fineAdjustDiv;
    }

    private int getAdjustment() {
        if (GuiScreen.isShiftKeyDown()) {
            return coarseIncr;
        } else if (GuiScreen.isCtrlKeyDown()) {
            return fineIncr;
        } else {
            return incr;
        }
    }

    private boolean adjustField(int adj) {
        int val;
        try {
            val = Integer.parseInt(getText());
        } catch (NumberFormatException e) {
            val = min;
        }
        setText("");
        writeText(Integer.toString(Math.max(min, Math.min(max, val + adj))));
        return true;
    }
}
