package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;

public class SpeedUpgrade extends UpgradeItem {
    @Override
    public Object[] getExtraUsageParams() {
        int maxUseful = (int) Math.ceil((ConfigHolder.common.router.baseTickRate.get() - ConfigHolder.common.router.hardMinTickRate.get()) / (double) ConfigHolder.common.router.ticksPerUpgrade.get());
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
