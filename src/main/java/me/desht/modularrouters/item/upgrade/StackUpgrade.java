package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;

import java.awt.*;

public class StackUpgrade extends Upgrade {
    @Override
    public Object[] getExtraUsageParams() {
        TileEntityItemRouter router = ModularRouters.proxy.getOpenItemRouter();
        int itemsPerTick = router == null ? 1 : router.getItemsPerTick();
        return new Object[] { itemsPerTick, 6 };
    }

    @Override
    public Color getItemTint() {
        return new Color(128, 223, 255);
    }
}
