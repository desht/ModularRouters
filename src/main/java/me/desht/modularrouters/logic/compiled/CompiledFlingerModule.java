package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public class CompiledFlingerModule extends CompiledDropperModule {
    public static final String NBT_SPEED = "Speed";
    public static final String NBT_PITCH = "Pitch";
    public static final String NBT_YAW = "Yaw";

    private final float speed, pitch, yaw;

    public CompiledFlingerModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        CompoundTag compound = ModuleHelper.validateNBT(stack);
        Stream.of(NBT_SPEED, NBT_PITCH, NBT_YAW)
                .filter(key -> !compound.contains(key))
                .forEach(key -> compound.putFloat(key, 0.0f));

        speed = compound.getFloat(NBT_SPEED);
        pitch = compound.getFloat(NBT_PITCH);
        yaw = compound.getFloat(NBT_YAW);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        boolean fired = super.execute(router);

        if (fired && MRConfig.Common.Module.flingerEffects) {
            ModuleTarget target = getTarget();
            int n = Math.round(speed * 5);
            BlockPos pos = target.gPos.pos();
            if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
                ((ServerLevel) router.getLevel()).sendParticles(ParticleTypes.LARGE_SMOKE,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, n,
                        0.0, 0.0, 0.0, 0.0);
            }
            router.playSound(null, pos, ModSounds.THUD.get(), SoundSource.BLOCKS, 0.5f + speed, 1.0f);
        }

        return fired;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    protected void setupItemVelocity(ModularRouterBlockEntity router, ItemEntity item) {
        Direction routerFacing = router.getAbsoluteFacing(ModuleItem.RelativeDirection.FRONT);
        float basePitch = 0.0f;
        float baseYaw;
        switch (getDirection()) {
            case UP:
                basePitch = 90.0f;
                baseYaw = yawFromFacing(routerFacing);
                break;
            case DOWN:
                basePitch = -90.0f;
                baseYaw = yawFromFacing(routerFacing);
                break;
            default:
                baseYaw = yawFromFacing(getFacing());
                break;
        }

        double yawRad = Math.toRadians(baseYaw + yaw), pitchRad = Math.toRadians(basePitch + pitch);

        double x = (Math.cos(yawRad) * Math.cos(pitchRad));   // east is positive X
        double y = Math.sin(pitchRad);
        double z = -(Math.sin(yawRad) * Math.cos(pitchRad));  // north is negative Z

        item.setDeltaMovement(x * speed, y * speed, z * speed);
    }

    private float yawFromFacing(Direction absoluteFacing) {
        switch (absoluteFacing) {
            case EAST: return 0.0f;
            case NORTH: return 90.0f;
            case WEST: return 180.0f;
            case SOUTH: return 270.0f;
        }
        return 0;
    }
}
