package me.desht.modularrouters.logic;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class CompiledSenderModuleSettings extends CompiledModuleSettings {
    private double particlePos = 0.0;

    public CompiledSenderModuleSettings(ItemStack stack) {
        super(stack);
    }

    public void resetParticlePos() {
        particlePos = 0.0;
    }

    public void playParticles(TileEntityItemRouter router, CompiledModuleSettings settings, BlockPos targetPos) {
        Vec3d vec1 = new Vec3d(router.getPos());
        Vec3d vec2 = new Vec3d(targetPos);
        Vec3d vec3 = vec2.subtract(vec1).scale(particlePos).add(vec1).addVector(0.5, 0.5, 0.5);
        ((WorldServer) router.getWorld()).spawnParticle(EnumParticleTypes.REDSTONE, false, vec3.xCoord, vec3.yCoord, vec3.zCoord, 2, 0.05, 0.05, 0.05, 0.001);
        particlePos += 0.1;
        if (particlePos > 1.0) {
            resetParticlePos();
        }
    }
}
