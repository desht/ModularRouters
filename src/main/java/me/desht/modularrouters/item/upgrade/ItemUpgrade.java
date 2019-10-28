package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.ItemBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public abstract class ItemUpgrade extends ItemBase implements ModItems.ITintable {

    public ItemUpgrade(Properties props) {
        super(props);
    }

    public TintColor getItemTint() {
        return TintColor.WHITE;
    }

    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        // no-op by default
    }

    protected void addExtraInformation(ItemStack stack, List<ITextComponent> list) {

    }
}
