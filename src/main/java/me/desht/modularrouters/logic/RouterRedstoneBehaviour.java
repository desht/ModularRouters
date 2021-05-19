package me.desht.modularrouters.logic;

import me.desht.modularrouters.client.util.IHasTranslationKey;

public enum RouterRedstoneBehaviour implements IHasTranslationKey {
    ALWAYS, LOW, HIGH, NEVER, PULSE;

    public static RouterRedstoneBehaviour forValue(String string) {
        try {
            return RouterRedstoneBehaviour.valueOf(string);
        } catch (IllegalArgumentException e) {
            return ALWAYS;
        }
    }

    public boolean shouldRun(boolean powered, boolean pulsed) {
        switch (this) {
            case ALWAYS: return true;
            case LOW: return !powered;
            case HIGH: return powered;
            case PULSE: return pulsed;
            default: return false;  // including NEVER
        }
    }

    @Override
    public String getTranslationKey() {
        return "modularrouters.guiText.tooltip.redstone." + this;
    }
}
