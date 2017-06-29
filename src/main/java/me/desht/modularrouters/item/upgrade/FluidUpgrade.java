package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class FluidUpgrade extends Upgrade {
    @Override
    public Object[] getExtraUsageParams() {
        return new Object[] { ConfigHandler.router.mBperFluidUpgrade };
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addUsageInformation(itemstack, player, list, advanced);
        TileEntityItemRouter router = ModularRouters.proxy.getOpenItemRouter();
        if (router != null) {
            list.add(I18n.format("itemText.usage.item.fluidUpgradeRouter", router.getFluidTransferRate()));
        }
    }

}
