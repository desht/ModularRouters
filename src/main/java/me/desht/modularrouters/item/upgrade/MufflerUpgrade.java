package me.desht.modularrouters.item.upgrade;

import java.awt.*;

public class MufflerUpgrade extends ItemUpgrade {
    public MufflerUpgrade(Properties props) {
        super(props);
    }

    @Override
    public Color getItemTint() {
        return new Color(255, 255, 195);
    }
}
