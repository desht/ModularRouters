package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.ParticleBeamMessage;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.PacketDistributor;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class CompiledPullerModule2 extends CompiledPullerModule1 {
    private static final Color particleColor = Color.BLUE;

    public CompiledPullerModule2(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    protected List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        return Collections.singletonList(TargetedModule.getTarget(stack, !router.getWorld().isRemote));
    }

    @Override
    boolean validateRange(TileEntityItemRouter router, ModuleTarget target) {
        return target != null &&
                MiscUtil.getDimensionForWorld(router.getWorld()) == target.dimId &&
                router.getPos().distanceSq(target.pos) <= getRangeSquared();
    }

    @Override
    protected void playParticles(TileEntityItemRouter router, BlockPos targetPos) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE) < 2) {
            Vec3d vec1 = new Vec3d(router.getPos()).add(0.5, 0.5, 0.5);
            Vec3d vec2 = new Vec3d(targetPos).add(0.5, 0.5, 0.5);
            PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(vec1.x, vec1.y, vec1.z, 32, router.getWorld().dimension.getType());
            PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp),
                    new ParticleBeamMessage(vec1.x, vec1.y, vec1.z, vec2.x, vec2.y, vec2.z, particleColor, 0.5f));
        }
    }
}
