package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.TintColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

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
    public TintColor getItemTint() {
        return new TintColor(128, 223, 255);
    }
}
