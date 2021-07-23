package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.ItemBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class ItemUpgrade extends ItemBase implements ModItems.ITintable {

    public ItemUpgrade() {
        super(ModItems.defaultProps());
    }

    public TintColor getItemTint() {
        return TintColor.WHITE;
    }

    public void onCompiled(ItemStack stack, ModularRouterBlockEntity router) {
        // no-op by default
    }

    protected void addExtraInformation(ItemStack stack, List<Component> list) {

    }

    /**
     * Get the maximum number of this upgrade that can be put in an upgrade slot
     * @param slot the slot number
     * @return the max number of upgrades
     */
    public int getStackLimit(int slot) {
        return 1;
    }
}
