package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.network.ParticleBeamMessage;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
    protected boolean isRangeLimited() {
        return false;
    }
}
