package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.network.ParticleBeamMessage;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.awt.*;

public class SenderModule3 extends SenderModule2 {
    @Override
    protected void playParticles(TileEntityItemRouter router, CompiledModuleSettings settings, BlockPos targetPos, float val) {
        double x = router.getPos().getX() + 0.5;
        double y = router.getPos().getY() + 0.5;
        double z = router.getPos().getZ() + 0.5;
        EnumFacing facing = router.getAbsoluteFacing(RelativeDirection.FRONT);
        double x2 = x + facing.getFrontOffsetX() * 1.5;
        double z2 = z + facing.getFrontOffsetZ() * 1.5;
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(router.getWorld().provider.getDimension(), x, y, z, 32);
        ModularRouters.network.sendToAllAround(new ParticleBeamMessage(x, y, z, x2, y, z2, Color.getHSBColor(0.83333f, 1.0f, 0.8f)), point);
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
