package me.desht.modularrouters.gui.widgets;

import net.minecraft.util.ResourceLocation;

public abstract class TexturedCyclerButton <T extends Enum<T>> extends TexturedButton {
    private T state;
    private final int len;

    public TexturedCyclerButton(int buttonId, int x, int y, int width, int height, ResourceLocation location, T initialVal) {
        super(buttonId, x, y, width, height);
        state = initialVal;
        len = initialVal.getClass().getEnumConstants().length;
    }

    public T getState() {
        return state;
    }

    public void cycle(boolean forward) {
        int b = state.ordinal();
        b += forward ? 1 : -1;

        if (b >= len) {
            b = 0;
        } else if (b < 0) {
            b = len - 1;
        }

        state = (T) state.getClass().getEnumConstants()[b];
    }
}
