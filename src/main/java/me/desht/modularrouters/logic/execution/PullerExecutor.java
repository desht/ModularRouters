package me.desht.modularrouters.logic.execution;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.module.AbstractModule;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class PullerExecutor extends ModuleExecutor {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStack bufferStack = router.getBufferItemStack();
        if (settings.getDirection() != AbstractModule.RelativeDirection.NONE
                && (bufferStack == null || bufferStack.stackSize < bufferStack.getMaxStackSize())) {
            EnumFacing facingOpposite = router.getAbsoluteFacing(settings.getDirection()).getOpposite();
            BlockPos pos = router.getRelativeBlockPos(settings.getDirection());
            TileEntity te = router.getWorld().getTileEntity(pos);
            if (te != null  && te.hasCapability(ModularRouters.ITEM_HANDLER_CAPABILITY, facingOpposite)) {
                IItemHandler handler = te.getCapability(ModularRouters.ITEM_HANDLER_CAPABILITY, facingOpposite);
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack toExtract = handler.extractItem(i, router.getItemsPerTick(), true);
                    if (toExtract != null && settings.getFilter().pass(toExtract)) {
                        ItemStack excess = router.getBuffer().insertItem(0, toExtract, false);
                        int taken = toExtract.stackSize - (excess == null ? 0 : excess.stackSize);
                        handler.extractItem(i, taken, false);
                        return taken > 0;
                    }
                }
            }
        }
        return false;
    }
}
