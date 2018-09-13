package me.desht.modularrouters.client.gui.widgets.button;

public abstract class RadioButton extends TexturedToggleButton {
    private final int groupId;

    public RadioButton(int id, int groupId, int x, int y, int width, int height, boolean toggled) {
        super(id, x, y, width, height, toggled);
        this.groupId = groupId;
    }
}
