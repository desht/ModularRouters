package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.SlotItemHandler;

public class FilterSlot extends SlotItemHandler {
    private final TileEntityItemRouter router;
    private final EntityPlayer player;
    private final EnumHand hand;
    private final boolean serverSide;
    private final int index;

    FilterSlot(FilterHandler itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.router = router;
        this.player = null;
        this.hand = null;
        serverSide = !router.getWorld().isRemote;
        this.index = index;
    }

    FilterSlot(FilterHandler itemHandler, EntityPlayer player, EnumHand hand, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.router = null;
        this.player = player;
        this.hand = hand;
        serverSide = !player.getEntityWorld().isRemote;
        this.index = index;
    }

    @Override
    public void putStack(ItemStack stack) {
        // avoid saving the filter handler unnecessarily
        FilterHandler handler = (FilterHandler) getItemHandler();
        if (!ItemStack.areItemStacksEqual(stack, handler.getStackInSlot(index))) {
            handler.setStackInSlot(index, stack);
            onSlotChanged();
        }
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();

        FilterHandler handler = (FilterHandler) getItemHandler();
        handler.save();

        if (player != null && hand != null) {
            player.setHeldItem(hand, handler.getHoldingItemStack());
        } else if (router != null && serverSide) {
            router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
        }
    }
}
