package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.network.ParticleBeamMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.awt.*;
import java.util.Random;

public class CompiledSenderModule3 extends CompiledSenderModule2 {
    private double y2 = -1.0;
    private final Random r = new Random();
    private static final Color particleColor = Color.MAGENTA;

    public CompiledSenderModule3(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean isRangeLimited() {
        return false;
    }

    @Override
    protected void playParticles(TileEntityItemRouter router, BlockPos targetPos) {
        if (router.getUpgradeCount(ItemUpgrade.UpgradeType.MUFFLER) < 2) {
            double x = router.getPos().getX() + 0.5;
            double y = router.getPos().getY() + 0.5;
            double z = router.getPos().getZ() + 0.5;
            EnumFacing facing = router.getAbsoluteFacing(Module.RelativeDirection.FRONT);
            double x2 = x + facing.getXOffset() * 1.2;
            y2 = (y2 < y - 2 || y2 > y + 2) ? y : y2 + (r.nextDouble() - 0.5) / 5.0;
            double z2 = z + facing.getZOffset() * 1.2;
            NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(router.getWorld().provider.getDimension(), x, y, z, 32);
            ModularRouters.network.sendToAllAround(new ParticleBeamMessage(x, y, z, x2, y2, z2, particleColor, 0.7f), point);
        }
    }
}
