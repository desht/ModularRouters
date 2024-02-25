package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.gui.ISendToServer;
import me.desht.modularrouters.util.TranslatableEnum;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public abstract class TexturedCyclerButton<T extends Enum<T> & TranslatableEnum> extends TexturedButton {
    private T state;
    private final int len;

    public TexturedCyclerButton(int x, int y, int width, int height, T initialVal, ISendToServer dataSyncer) {
        super(x, y, width, height, button -> {
            ((TexturedCyclerButton<?>) button).cycle(!Screen.hasShiftDown());
            dataSyncer.sendToServer();
        });
        setState(initialVal);
        len = initialVal.getClass().getEnumConstants().length;
    }

    public T getState() {
        return state;
    }

    public void setState(T newState) {
        state = newState;
        setTooltip(state.getTranslationKey() == null ? null : makeTooltip(state));
    }

    protected Tooltip makeTooltip(T object) {
        return Tooltip.create(xlate(object.getTranslationKey()));
    }

    public void cycle(boolean forward) {
        int b = state.ordinal();
        b += forward ? 1 : -1;

        if (b >= len) {
            b = 0;
        } else if (b < 0) {
            b = len - 1;
        }

        //noinspection unchecked
        setState((T) state.getClass().getEnumConstants()[b]);  // should be a safe cast here; state is of class T
    }
}
