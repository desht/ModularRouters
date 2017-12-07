package me.desht.modularrouters.client.gui.widgets.button;

public interface ToggleButton {
    void toggle();
    void setToggled(boolean toggled);
    boolean isToggled();
    default boolean sendToServer() { return true; }
}
