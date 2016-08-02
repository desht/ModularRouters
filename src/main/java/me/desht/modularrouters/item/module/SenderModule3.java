package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class SenderModule3 extends SenderModule2 {
    @Override
    protected void playParticles(TileEntityItemRouter router, CompiledModuleSettings settings, BlockPos targetPos) {
        double x = router.getPos().getX() - 0.1 + Math.random() * 1.2;
        double y = router.getPos().getY() + 0.2 + Math.random() * 0.8;
        double z = router.getPos().getZ() - 0.1 + Math.random() * 1.2;

        ((WorldServer) router.getWorld()).spawnParticle(EnumParticleTypes.PORTAL, false, x, y, z, 0, 1.0D, 0.0D, 0.0D, 1.0D);
    }

    @Override
    protected TargetValidation validateTarget(TileEntityItemRouter router, TargetedSender.DimensionPos dimPos) {
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
}
