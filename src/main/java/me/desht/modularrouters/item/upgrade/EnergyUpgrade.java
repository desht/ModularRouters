package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.modularrouters.util.MiscUtil.commify;

public class EnergyUpgrade extends ItemUpgrade {
    private static final TintColor TINT_COLOR = new TintColor(128, 128, 128);

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    protected Object[] getExtraUsageParams() {
        return new Object[] {
                commify(MRConfig.Common.Router.fePerEnergyUpgrade),
                commify(MRConfig.Common.Router.feXferPerEnergyUpgrade)
        };
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addUsageInformation(itemstack, list);
        TileEntityItemRouter router = ClientUtil.getOpenItemRouter();
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
