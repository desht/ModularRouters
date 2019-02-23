package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;

import java.awt.*;

public class SpeedUpgrade extends ItemUpgrade {
    public SpeedUpgrade(Properties props) {
        super(props);
    }

    @Override
    public Object[] getExtraUsageParams() {
        int maxUseful = (int) Math.ceil((ConfigHandler.ROUTER.baseTickRate.get() - ConfigHandler.ROUTER.hardMinTickRate.get()) / (double) ConfigHandler.ROUTER.ticksPerUpgrade.get());
        TileEntityItemRouter router = ModularRouters.proxy.getOpenItemRouter();
        int tickRate = router == null ? 20 : router.getTickRate();
        return new Object[] { tickRate / 20.0f, tickRate, maxUseful };
    }

    @Override
    public Color getItemTint() {
        return new Color(224, 32, 32);
    }
}
