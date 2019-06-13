package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.gui.ISendToServer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

public class ItemStackCyclerButton<T extends Enum<T>> extends ItemStackButton {
    private T state;
    private final int len;
    private final ItemStack[] stacks;

    public ItemStackCyclerButton(int x, int y, int width, int height, boolean flat, ItemStack[] stacks, T initialVal, ISendToServer dataSyncer) {
        super(x, y, width, height, null, flat, button -> {
            ((ItemStackCyclerButton) button).cycle(!Screen.hasShiftDown());
            dataSyncer.sendToServer();
        });
        state = initialVal;
        len = initialVal.getClass().getEnumConstants().length;
        if (stacks.length != len) {
            throw new IllegalArgumentException("stacks parameter must have length=" + len);
        }
        this.stacks = stacks;
    }

    public T getState() {
        return state;
    }

    public void setState(T newState) { state = newState; }

    public T cycle(boolean forward) {
        int b = state.ordinal();

        T newState;
        do {
            b += forward ? 1 : -1;
            if (b >= len) {
                b = 0;
            } else if (b < 0) {
                b = len - 1;
            }
            newState = (T) state.getClass().getEnumConstants()[b];
        } while (!isApplicable(newState) && b != state.ordinal());

        setState(newState);
        return state;
    }

    public boolean isApplicable(T state) {
        return true;
    }

    @Override
    public ItemStack getRenderStack() {
        return stacks[state.ordinal()];
    }
}
