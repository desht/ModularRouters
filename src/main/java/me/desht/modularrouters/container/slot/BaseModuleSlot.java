package me.desht.modularrouters.container.slot;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.BulkFilterHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.SlotItemHandler;

public abstract class BaseModuleSlot<T extends BaseModuleHandler> extends SlotItemHandler {
    private final TileEntityItemRouter router;
    private final EntityPlayer player;
    private final EnumHand hand;
    private final boolean serverSide;
    private final int index;

    public BaseModuleSlot(T itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.router = router;
        this.player = null;
        this.hand = null;
        serverSide = !router.getWorld().isRemote;
        this.index = index;
    }

    public BaseModuleSlot(T itemHandler, EntityPlayer player, EnumHand hand, int index, int xPosition, int yPosition) {
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
        T handler = (T) getItemHandler();
        System.out.println("put stack: " + stack + " - stack in slot: " + handler.getStackInSlot(index));
        if (!ItemStack.areItemStacksEqual(stack, handler.getStackInSlot(index))) {
            System.out.println(" - make the change!");
            handler.setStackInSlot(index, stack);
            onSlotChanged();
        }
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();

        T handler = (T) getItemHandler();
        handler.save();

        if (player != null && hand != null) {
            player.setHeldItem(hand, handler.getHolderStack());
        } else if (router != null && serverSide) {
            router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
        }
    }

    public static class ModuleFilterSlot extends BaseModuleSlot<ModuleFilterHandler> {
        public ModuleFilterSlot(ModuleFilterHandler itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
            super(itemHandler, router, index, xPosition, yPosition);
        }

        public ModuleFilterSlot(ModuleFilterHandler itemHandler, EntityPlayer player, EnumHand hand, int index, int xPosition, int yPosition) {
            super(itemHandler, player, hand, index, xPosition, yPosition);
        }
    }

    public static class BulkFilterSlot extends BaseModuleSlot<BulkFilterHandler> {
        public BulkFilterSlot(BulkFilterHandler itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
            super(itemHandler, router, index, xPosition, yPosition);
        }

        public BulkFilterSlot(BulkFilterHandler itemHandler, EntityPlayer player, EnumHand hand, int index, int xPosition, int yPosition) {
            super(itemHandler, player, hand, index, xPosition, yPosition);
        }
    }
}
