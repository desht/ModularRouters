package me.desht.modularrouters.logic;

public enum RouterRedstoneBehaviour {
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

    public String getTranslationKey() {
        return "modularrouters.guiText.tooltip.redstone." + toString();
    }
}
