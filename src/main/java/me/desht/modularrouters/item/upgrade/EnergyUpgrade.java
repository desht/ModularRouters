package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;
import static me.desht.modularrouters.util.MiscUtil.commify;

public class EnergyUpgrade extends UpgradeItem {
    private static final TintColor TINT_COLOR = new TintColor(54, 1, 61);

    @Override
    public TintColor getItemTint() {
        return new TintColor(79, 9, 90);
    }

    @Override
    protected Object[] getExtraUsageParams() {
        return new Object[] {
                commify(ConfigHolder.common.router.fePerEnergyUpgrade.get()),
                commify(ConfigHolder.common.router.feXferPerEnergyUpgrade.get())
        };
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, List<Component> list) {
        super.addUsageInformation(itemstack, list);
        ClientUtil.getOpenItemRouter().ifPresent(router -> {
            list.add(xlate("modularrouters.itemText.usage.item.energyUpgradeRouter",
                    commify(router.getEnergyCapacity()), commify(router.getEnergyXferRate())));
        });
    }

    @Override
    public int getStackLimit(int slot) {
        return 64;
    }
}
