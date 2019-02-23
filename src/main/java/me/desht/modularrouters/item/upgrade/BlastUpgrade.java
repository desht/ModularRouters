package me.desht.modularrouters.item.upgrade;

import java.awt.*;

public class BlastUpgrade extends ItemUpgrade {
    public BlastUpgrade(Properties props) {
        super(props);
    }

    @Override
    public Color getItemTint() {
        return new Color(144, 0, 0);
    }
}
