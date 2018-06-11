package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.ParticleBeamMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class CompiledPullerModule2 extends CompiledPullerModule {
    private static final Color particleColor = Color.BLUE;

    public CompiledPullerModule2(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    protected List<ModuleTarget> setupTarget(TileEntityItemRouter router, ItemStack stack) {
        return Collections.singletonList(TargetedModule.getTarget(stack, !router.getWorld().isRemote));
    }

    @Override
    boolean validateRange(TileEntityItemRouter router, ModuleTarget target) {
        return router.getWorld().provider.getDimension() == target.dimId &&
                router.getPos().distanceSq(target.pos) <= getRangeSquared();
    }

    @Override
    protected void playParticles(TileEntityItemRouter router, BlockPos targetPos) {
        if (router.getUpgradeCount(ItemUpgrade.UpgradeType.MUFFLER) < 2) {
            Vec3d vec1 = new Vec3d(router.getPos()).addVector(0.5, 0.5, 0.5);
            Vec3d vec2 = new Vec3d(targetPos).addVector(0.5, 0.5, 0.5);
            NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(router.getWorld().provider.getDimension(), vec1.x, vec1.y, vec1.z, 32);
            ModularRouters.network.sendToAllAround(new ParticleBeamMessage(vec1.x, vec1.y, vec1.z, vec2.x, vec2.y, vec2.z, particleColor, 0.5f), point);
        }
    }
}
