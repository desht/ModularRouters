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
    public boolean isValidTarget(TileEntityItemRouter router, TargetedSender.DimensionPos dimPos) {
        WorldServer w = DimensionManager.getWorld(dimPos.dimId);
        return w != null && w.getChunkProvider().chunkExists(dimPos.pos.getX() >> 4, dimPos.pos.getZ() >> 4);
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new Sender3Executor();
    }
}
