package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class UpgradeSlot extends SlotItemHandler {
    private final TileEntityItemRouter router;

    public UpgradeSlot(TileEntityItemRouter router, IItemHandler inventory, int slot, int xpos, int ypos) {
        super(inventory, slot, xpos, ypos);
        this.router = router;
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        router.recompileNeeded();
    }

    @Override
    public boolean isItemValid(ItemStack itemstack) {
        return itemstack.getItem() instanceof ItemUpgrade;
    }
}
