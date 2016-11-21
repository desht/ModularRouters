package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

class BufferSlot extends SlotItemHandler {
    private final TileEntityItemRouter router;

    BufferSlot(TileEntityItemRouter router, int index, int xPosition, int yPosition) {
        super(router.getBuffer(), index, xPosition, yPosition);
        this.router = router;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return true;
    }

    @Override
    public void onSlotChanged() {
        // unfortunately we can't leave this entirely to BufferHandler#onSlotChanged()
        // seems like that doesn't always get called at the right time
        router.getWorld().updateComparatorOutputLevel(router.getPos(), router.getBlockType());
    }
}
