package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FluidUpgrade extends UpgradeItem {
    @Override
    public Object[] getExtraUsageParams() {
        return new Object[] { MRConfig.Common.Router.mBperFluidUpgade };
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, List<Component> list) {
        super.addUsageInformation(itemstack, list);
        ClientUtil.getOpenItemRouter()
                .ifPresent(router -> list.add(ClientUtil.xlate("modularrouters.itemText.usage.item.fluidUpgradeRouter", router.getFluidTransferRate())));
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(84, 138, 255);
    }

    @Override
    public int getStackLimit(int slot) {
        return 35;
    }
}
