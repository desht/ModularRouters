package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.awt.*;
import java.util.List;

public class FluidUpgrade extends ItemUpgrade {
    public FluidUpgrade(Properties props) {
        super(props);
    }

    @Override
    public Object[] getExtraUsageParams() {
        return new Object[] { ConfigHandler.ROUTER.mBperFluidUpgade.get() };
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addUsageInformation(itemstack, list);
        TileEntityItemRouter router = ModularRouters.proxy.getOpenItemRouter();
        if (router != null) {
            list.add(new TextComponentTranslation("itemText.usage.item.fluidUpgradeRouter", router.getFluidTransferRate()));
        }
    }

    @Override
    public Color getItemTint() {
        return new Color(84, 138, 255);
    }
}
