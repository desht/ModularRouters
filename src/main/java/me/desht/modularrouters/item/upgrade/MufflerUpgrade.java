package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.client.util.TintColor;

public class MufflerUpgrade extends ItemUpgrade {
    public MufflerUpgrade(Properties props) {
        super(props);
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(255, 255, 195);
    }
}
