package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.gui.ISendToServer;
import net.minecraft.client.gui.screen.Screen;

public abstract class TexturedCyclerButton<T extends Enum<T>> extends TexturedButton {
    private T state;
    private final int len;

    public TexturedCyclerButton(int x, int y, int width, int height, T initialVal, ISendToServer dataSyncer) {
        super(x, y, width, height, button -> {
            ((TexturedCyclerButton) button).cycle(!Screen.hasShiftDown());
            dataSyncer.sendToServer();
        });
        state = initialVal;
        len = initialVal.getClass().getEnumConstants().length;
    }

    public T getState() {
        return state;
    }

    public void setState(T newState) { state = newState; }

    public T cycle(boolean forward) {
        int b = state.ordinal();
        b += forward ? 1 : -1;

        if (b >= len) {
            b = 0;
        } else if (b < 0) {
            b = len - 1;
        }

        state = (T) state.getClass().getEnumConstants()[b];
        return state;
    }

}
