package me.desht.modularrouters.container.slot;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.BulkFilterHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.items.SlotItemHandler;

import static me.desht.modularrouters.block.tile.TileEntityItemRouter.COMPILE_MODULES;

public abstract class BaseModuleSlot<T extends BaseModuleHandler> extends SlotItemHandler {
    private final TileEntityItemRouter router;
    private final int index;

    public BaseModuleSlot(T itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.router = router;
        this.index = index;
    }

    @Override
    public void putStack(ItemStack stack) {
        // bit of a hack, but ensures bulk item filter NBT is properly init'd
        if (stack.getItem() instanceof BulkItemFilter) {
            CompoundNBT compound = stack.getChildTag(ModularRouters.MODID);
            if (compound == null || !compound.contains(ModuleHelper.NBT_FILTER)) {
                stack.getOrCreateChildTag(ModularRouters.MODID).put(ModuleHelper.NBT_FILTER, new ListNBT());
            }
        }

        // avoid saving the filter handler unnecessarily
        T handler = (T) getItemHandler();
        if (handler.isItemValid(index, stack) && !ItemStack.areItemStacksEqual(stack, handler.getStackInSlot(index))) {
            handler.setStackInSlot(index, stack);
            onSlotChanged();
        }
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();

        T handler = (T) getItemHandler();
        handler.save();

        if (router != null) router.recompileNeeded(COMPILE_MODULES);
    }

    public static class ModuleFilterSlot extends BaseModuleSlot<ModuleFilterHandler> {
        public ModuleFilterSlot(ModuleFilterHandler itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
            super(itemHandler, router, index, xPosition, yPosition);
        }

        public ModuleFilterSlot(ModuleFilterHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, null, index, xPosition, yPosition);
        }
    }

    public static class BulkFilterSlot extends BaseModuleSlot<BulkFilterHandler> {
        public BulkFilterSlot(BulkFilterHandler itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
            super(itemHandler, router, index, xPosition, yPosition);
        }

        public BulkFilterSlot(BulkFilterHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, null, index, xPosition, yPosition);
        }
    }
}
