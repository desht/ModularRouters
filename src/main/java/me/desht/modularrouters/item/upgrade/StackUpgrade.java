package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;
import java.util.List;

public class StackUpgrade extends ItemUpgrade {
    public StackUpgrade(Properties props) {
        super(props);
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<ITextComponent> list) {
    }

    @Override
    public Object[] getExtraUsageParams() {
        TileEntityItemRouter router = ModularRouters.proxy.getOpenItemRouter();
        int itemsPerTick = router == null ? 1 : router.getItemsPerTick();
        return new Object[] { itemsPerTick, 6 };
    }

    @Override
    public Color getItemTint() {
        return new Color(128, 223, 255);
    }
}
