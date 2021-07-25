package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig.Common.Router;

public class SpeedUpgrade extends UpgradeItem {
    @Override
    public Object[] getExtraUsageParams() {
        int maxUseful = (int) Math.ceil((Router.baseTickRate - Router.hardMinTickRate) / (double) Router.ticksPerUpgrade);
        return ClientUtil.getOpenItemRouter().map(router -> {
            int tickRate = router.getTickRate();
            return new Object[] { tickRate / 20.0f, tickRate, maxUseful };
        }).orElse(new Object[] { 1f, 20, maxUseful});
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(224, 32, 32);
    }

    @Override
    public int getStackLimit(int slot) {
        return 9;
    }
}
