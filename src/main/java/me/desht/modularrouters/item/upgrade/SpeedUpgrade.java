package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;

public class SpeedUpgrade extends Upgrade {
    @Override
    public Object[] getExtraUsageParams() {
        int maxUseful = (int) Math.ceil((ConfigHandler.router.baseTickRate - ConfigHandler.router.hardMinTickRate) / (double) ConfigHandler.router.ticksPerUpgrade);
        TileEntityItemRouter router = ModularRouters.proxy.getOpenItemRouter();
        int tickRate = router == null ? 20 : router.getTickRate();
        return new Object[] { tickRate / 20.0f, tickRate, maxUseful };
    }

}
