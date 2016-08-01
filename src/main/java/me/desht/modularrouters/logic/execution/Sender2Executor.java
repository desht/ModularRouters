package me.desht.modularrouters.logic.execution;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemSenderModule2;
import me.desht.modularrouters.item.module.TargetedSender;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.items.IItemHandler;

public class Sender2Executor extends Sender1Executor {
    @Override
    protected SenderTarget findTargetInventory(TileEntityItemRouter router, CompiledModuleSettings settings) {
        TargetedSender.DimensionPos target = settings.getTarget();
        ItemSenderModule2 module = (ItemSenderModule2) settings.getModule();

        if (!module.validateTarget(router, target).isOK()) {
            return null;
        }

        WorldServer w = DimensionManager.getWorld(target.dimId);
        IItemHandler handler = InventoryUtils.getInventory(w, target.pos, target.face);
        return handler == null ? null : new SenderTarget(target.pos, handler);
    }
}
