package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraftforge.items.SlotItemHandler;

public class FilterSlot extends SlotItemHandler {
    private final FilterHandler filterHandler;
    private final TileEntityItemRouter router;

    public FilterSlot(FilterHandler itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        filterHandler = itemHandler;
        this.router = router;
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        filterHandler.save();
        if (router != null) {
            router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
        }
    }
}
