package me.desht.modularrouters.mixin;

import me.desht.modularrouters.util.fake_player.RouterFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    public void modularrouters$levelEvent(Entity source, int type, BlockPos pos, int data, CallbackInfo cir) {
        // we'll handle block-break particles ourselves since they can be suppressed by muffler upgrades
        if (source instanceof RouterFakePlayer && !source.level().isClientSide() && type == LevelEvent.PARTICLES_DESTROY_BLOCK) {
            cir.cancel();
        }
    }
}
