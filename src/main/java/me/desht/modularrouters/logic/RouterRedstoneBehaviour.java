package me.desht.modularrouters.logic;

import me.desht.modularrouters.util.TranslatableEnum;

public enum RouterRedstoneBehaviour implements TranslatableEnum {
    ALWAYS, LOW, HIGH, NEVER, PULSE;

    public static RouterRedstoneBehaviour forValue(String string) {
        try {
            return RouterRedstoneBehaviour.valueOf(string);
        } catch (IllegalArgumentException e) {
            return ALWAYS;
        }
    }

    public boolean shouldRun(boolean powered, boolean pulsed) {
        return switch (this) {
            case ALWAYS -> true;
            case LOW -> !powered;
            case HIGH -> powered;
            case PULSE -> pulsed;
            case NEVER -> false;
        };
    }

    @Override
    public String getTranslationKey() {
        return "modularrouters.guiText.tooltip.redstone." + this;
    }
}
