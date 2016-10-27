package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.SenderModule2;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.items.IItemHandler;

public class CompiledSenderModule2 extends CompiledSenderModule1 {
    public CompiledSenderModule2(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    protected ModuleTarget setupTarget(TileEntityItemRouter router, ItemStack stack) {
        return TargetedModule.getTarget(stack);
    }

    @Override
    protected PositionedItemHandler findTargetInventory(TileEntityItemRouter router) {
        ModuleTarget target = getTarget();
        if (target == null || !validate(router, target)) {
            return null;
        }

        WorldServer w = DimensionManager.getWorld(target.dimId);
        if (w.getChunkProvider().chunkExists(target.pos.getX() >> 4, target.pos.getZ() >> 4)) {
            IItemHandler handler = InventoryUtils.getInventory(w, target.pos, target.face);
            return handler == null ? null : new PositionedItemHandler(target.pos, handler);
        }

        return null;
    }

    protected boolean validate(TileEntityItemRouter router, ModuleTarget target) {
        return !(isRangeLimited() &&
                (router.getWorld().provider.getDimension() != target.dimId
                || router.getPos().distanceSq(target.pos) > SenderModule2.maxDistanceSq(router)));

    }

    public boolean isRangeLimited() {
        return true;
    }

}
