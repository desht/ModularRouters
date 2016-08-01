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

        if (!module.isValidTarget(router, target)) {
            return null;
        }

        WorldServer w = DimensionManager.getWorld(target.dimId);
        IItemHandler handler = InventoryUtils.getInventory(w, target.pos, target.face);
        if (handler != null) {
            return new SenderTarget(target.pos, handler);
        }
        return null;
    }
}
