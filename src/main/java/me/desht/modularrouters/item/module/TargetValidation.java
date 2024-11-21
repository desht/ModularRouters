package me.desht.modularrouters.item.module;

import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

public enum TargetValidation implements StringRepresentable {
    OK("ok"),
    OUT_OF_RANGE("out_of_range"),
    NOT_LOADED("not_loaded"),
    NOT_INVENTORY("no_inventory"),
    BAD_DIMENSION("bad_dimension");

    private final String name;

    TargetValidation(String name) {
        this.name = name;
    }

    public ChatFormatting getColor() {
        return this == OK ? ChatFormatting.GREEN : ChatFormatting.RED;
    }

    public String translationKey() {
        return "modularrouters.chatText.targetValidation." + getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
