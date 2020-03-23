package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHandler;

public class SpeedUpgrade extends ItemUpgrade {
    @Override
    public Object[] getExtraUsageParams() {
        int maxUseful = (int) Math.ceil((ConfigHandler.ROUTER.baseTickRate.get() - ConfigHandler.ROUTER.hardMinTickRate.get()) / (double) ConfigHandler.ROUTER.ticksPerUpgrade.get());
        TileEntityItemRouter router = ClientUtil.getOpenItemRouter();
        int tickRate = router == null ? 20 : router.getTickRate();
        return new Object[] { tickRate / 20.0f, tickRate, maxUseful };
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(224, 32, 32);
    }
}
