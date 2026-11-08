package me.desht.modularrouters.logic.compiled;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.item.module.FlingerModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class CompiledFlingerModule extends CompiledDropperModule {
    private final FlingerSettings settings;

    public CompiledFlingerModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        settings = stack.getOrDefault(ModDataComponents.FLINGER_SETTINGS, FlingerSettings.DEFAULT).validate();
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        boolean fired = super.execute(router);

        if (fired && ConfigHolder.common.module.flingerEffects.get()) {
            ModuleTarget target = getTarget();
            int n = Math.round(getSpeed() * 5);
            BlockPos pos = target.gPos.pos();
            if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2 && router.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, n,
                        0.0, 0.0, 0.0, 0.0);
            }
            router.playSound(null, pos, ModSounds.THUD.get(), SoundSource.BLOCKS, 0.5f + getSpeed(), 1.0f);
        }

        return fired;
    }

    public float getYaw() {
        return settings.yaw;
    }

    public float getPitch() {
        return settings.pitch;
    }

    public float getSpeed() {
        return settings.speed;
    }

    @Override
    protected void setupItemVelocity(ModularRouterBlockEntity router, ItemEntity item) {
        Direction routerFacing = router.getAbsoluteFacing(RelativeDirection.FRONT);
        float basePitch = 0.0f;
        float baseYaw;
        switch (getDirection()) {
            case UP -> {
                basePitch = 90.0f;
                baseYaw = yawFromFacing(routerFacing);
            }
            case DOWN -> {
                basePitch = -90.0f;
                baseYaw = yawFromFacing(routerFacing);
            }
            default -> baseYaw = yawFromFacing(getAbsoluteFacing());
        }

        double yawRad = Math.toRadians(baseYaw + getYaw()), pitchRad = Math.toRadians(basePitch + getPitch());

        double x = (Math.cos(yawRad) * Math.cos(pitchRad));   // east is positive X
        double y = Math.sin(pitchRad);
        double z = -(Math.sin(yawRad) * Math.cos(pitchRad));  // north is negative Z

        item.setDeltaMovement(new Vec3(x, y, z).scale(getSpeed()));
    }

    private float yawFromFacing(Direction absoluteFacing) {
        return switch (absoluteFacing) {
            case EAST -> 0.0f;
            case NORTH -> 90.0f;
            case WEST -> 180.0f;
            case SOUTH -> 270.0f;
            default -> 0;
        };
    }

    public record FlingerSettings(float speed, float pitch, float yaw) {
        public static final FlingerSettings DEFAULT = new FlingerSettings(0f, 0f, 0f);

        public static final Codec<FlingerSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.FLOAT.fieldOf("speed").forGetter(FlingerSettings::speed),
                Codec.FLOAT.fieldOf("pitch").forGetter(FlingerSettings::pitch),
                Codec.FLOAT.optionalFieldOf("yaw", 0f).forGetter(FlingerSettings::yaw)
        ).apply(builder, FlingerSettings::new));

        public static StreamCodec<FriendlyByteBuf, FlingerSettings> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, FlingerSettings::speed,
                ByteBufCodecs.FLOAT, FlingerSettings::pitch,
                ByteBufCodecs.FLOAT, FlingerSettings::yaw,
                FlingerSettings::new
        );

        private FlingerSettings validate() {
            if (validateSetting(speed, FlingerModule.MIN_SPEED, FlingerModule.MAX_SPEED)
                    && validateSetting(pitch, FlingerModule.MIN_PITCH, FlingerModule.MAX_PITCH)
                    && validateSetting(yaw, FlingerModule.MIN_YAW, FlingerModule.MAX_YAW))
            {
                return this;
            }
            return DEFAULT;
        }

        private boolean validateSetting(float val, float min, float max) {
            return val >= min && val <= max && !Float.isInfinite(val);
        }
    }
}
