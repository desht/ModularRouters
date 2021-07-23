package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.client.util.TintColor;

public class MufflerUpgrade extends UpgradeItem {
    @Override
    public TintColor getItemTint() {
        return new TintColor(255, 255, 195);
    }

    @Override
    public int getStackLimit(int slot) {
        return 3;
    }
}
