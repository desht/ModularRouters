package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ITintable;
import me.desht.modularrouters.item.ItemBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;
import java.util.List;

public abstract class ItemUpgrade extends ItemBase implements ITintable {

    public ItemUpgrade(Properties props) {
        super(props);
    }

    public Color getItemTint() {
        return Color.WHITE;
    }

    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        // no-op by default
    }

    protected void addExtraInformation(ItemStack stack, List<ITextComponent> list) {

    }
}
