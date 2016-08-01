package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.logic.execution.Sender3Executor;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class ItemSenderModule3 extends ItemSenderModule2 {
    public ItemSenderModule3() {
        super("senderModule3");
    }

    @Override
    public TargetValidation validateTarget(TileEntityItemRouter router, TargetedSender.DimensionPos dimPos) {
        if (router == null) {
            return TargetValidation.ROUTER_MISSING;
        }
        WorldServer w = DimensionManager.getWorld(dimPos.dimId);
        if (w == null || !w.getChunkProvider().chunkExists(dimPos.pos.getX() >> 4, dimPos.pos.getZ() >> 4)) {
            return TargetValidation.NOT_LOADED;
        }
        if (w.getTileEntity(dimPos.pos) == null) {
            return TargetValidation.NOT_INVENTORY;
        }
        return TargetValidation.OK;
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new Sender3Executor();
    }
}
