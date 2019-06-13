package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.gui.ISendToServer;

public abstract class RadioButton extends TexturedToggleButton {
    private final int groupId;

    public RadioButton(int groupId, int x, int y, int width, int height, boolean toggled, ISendToServer dataSyncer) {
        super(x, y, width, height, toggled, dataSyncer);
        this.groupId = groupId;
    }
}
