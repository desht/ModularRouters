package me.desht.modularrouters.logic;

public enum RouterRedstoneBehaviour {
    ALWAYS, LOW, HIGH, NEVER, PULSE;

    public boolean shouldRun(boolean powered, boolean pulsed) {
        switch (this) {
            case ALWAYS: return true;
            case LOW: return !powered;
            case HIGH: return powered;
            case PULSE: return pulsed;
            case NEVER: return false;
            default: return false;
        }
    }
}
