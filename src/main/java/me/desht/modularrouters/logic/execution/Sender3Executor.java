package me.desht.modularrouters.logic.execution;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.TargetedSender;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.items.IItemHandler;

public class Sender3Executor extends Sender2Executor {
    @Override
    protected SenderTarget findTargetInventory(TileEntityItemRouter router, CompiledModuleSettings settings) {
        TargetedSender.DimensionPos target = settings.getTarget();
        WorldServer w = DimensionManager.getWorld(target.dimId);
        if (w != null) {
            IItemHandler handler = InventoryUtils.getInventory(w, target.pos, target.face);
            if (handler != null) {
                return new SenderTarget(target.pos, handler);
            }
        }
        return null;
    }

    @Override
    protected void playParticles(TileEntityItemRouter router, CompiledModuleSettings settings, BlockPos targetPos) {
        double x = router.getPos().getX() - 0.1 + Math.random() * 1.2;
        double y = router.getPos().getY() + 0.2 + Math.random() * 0.8;
        double z = router.getPos().getZ() - 0.1 + Math.random() * 1.2;

        ((WorldServer) router.getWorld()).spawnParticle(EnumParticleTypes.PORTAL, false, x, y, z, 0, 1.0D, 0.0D, 0.0D, 1.0D);
    }
}
