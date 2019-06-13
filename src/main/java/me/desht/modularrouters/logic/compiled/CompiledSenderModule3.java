package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.ParticleBeamMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.PacketDistributor;

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
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE) < 2) {
            double x = router.getPos().getX() + 0.5;
            double y = router.getPos().getY() + 0.5;
            double z = router.getPos().getZ() + 0.5;
            Direction facing = router.getAbsoluteFacing(ItemModule.RelativeDirection.FRONT);
            double x2 = x + facing.getXOffset() * 1.2;
            y2 = (y2 < y - 2 || y2 > y + 2) ? y : y2 + (r.nextDouble() - 0.5) / 5.0;
            double z2 = z + facing.getZOffset() * 1.2;
            PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(x, y, z, 32, router.getWorld().dimension.getType());
            PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp),
                    new ParticleBeamMessage(x, y, z, x2, y2, z2, particleColor, 0.5f));
        }
    }
}
