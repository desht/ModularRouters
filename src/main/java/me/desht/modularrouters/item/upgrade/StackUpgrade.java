package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;

public class StackUpgrade extends ItemUpgrade {
    @Override
    public Object[] getExtraUsageParams() {
        ModularRouterBlockEntity router = ClientUtil.getOpenItemRouter();
        int itemsPerTick = router == null ? 1 : router.getItemsPerTick();
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
