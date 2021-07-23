package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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
                commify(MRConfig.Common.Router.fePerEnergyUpgrade),
                commify(MRConfig.Common.Router.feXferPerEnergyUpgrade)
        };
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, List<Component> list) {
        super.addUsageInformation(itemstack, list);
        ModularRouterBlockEntity router = ClientUtil.getOpenItemRouter();
        if (router != null) {
            list.addAll(GuiUtil.xlateAndSplit("modularrouters.itemText.usage.item.energyUpgradeRouter",
                    commify(router.getEnergyCapacity()), commify(router.getEnergyXferRate())));
        }
    }

    @Override
    public int getStackLimit(int slot) {
        return 64;
    }
}
