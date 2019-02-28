package me.desht.modularrouters.container.slot;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraftforge.items.SlotItemHandler;

public class BufferSlot extends SlotItemHandler {
    private final TileEntityItemRouter router;

    public BufferSlot(TileEntityItemRouter router, int index, int xPosition, int yPosition) {
        super(router.getBuffer(), index, xPosition, yPosition);
        this.router = router;
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        // unfortunately we can't leave this entirely to BufferHandler#onSlotChanged()
        // seems like that doesn't always get called at the right time
        router.getWorld().updateComparatorOutputLevel(router.getPos(), router.getBlockState().getBlock());
    }
}
