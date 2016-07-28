package me.desht.modularrouters.container;

import me.desht.modularrouters.item.module.AbstractModule;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ModuleSlot extends SlotItemHandler {
    private final TileEntityItemRouter router;

    public ModuleSlot(TileEntityItemRouter router, IItemHandler inv, int index, int xpos, int ypos) {
        super(inv, index, xpos, ypos);
        this.router = router;
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        router.recompileNeeded();
    }

    @Override
	public boolean isItemValid(ItemStack itemstack) {
        return itemstack.getItem() instanceof AbstractModule;
	}
}
