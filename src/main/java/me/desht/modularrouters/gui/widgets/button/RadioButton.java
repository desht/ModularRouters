package me.desht.modularrouters.gui.widgets.button;

public abstract class RadioButton extends TexturedToggleButton {
    private final int groupId;

    public RadioButton(int id, int groupId, int x, int y, int width, int height) {
        super(id, x, y, width, height);
        this.groupId = groupId;
    }
}
