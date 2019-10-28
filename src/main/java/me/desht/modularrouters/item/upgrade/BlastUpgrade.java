package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.client.util.TintColor;

public class BlastUpgrade extends ItemUpgrade {
    public BlastUpgrade(Properties props) {
        super(props);
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(144, 0, 0);
    }
}
