package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;

public class StackUpgrade extends UpgradeItem {
    @Override
    public Object[] getExtraUsageParams() {
        int itemsPerTick = ClientUtil.getOpenItemRouter().map(ModularRouterBlockEntity::getItemsPerTick).orElse(1);
        return new Object[] { itemsPerTick, 6 };
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(128, 223, 255);
    }

    @Override
    public int getStackLimit(int slot) {
        return 6;
    }
}
