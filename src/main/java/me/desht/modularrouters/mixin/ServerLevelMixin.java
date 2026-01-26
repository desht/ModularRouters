package me.desht.modularrouters.mixin;

import me.desht.modularrouters.util.fake_player.RouterFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data, CallbackInfo cir) {
        // we'll handle block-break particles ourselves since they can be suppressed by muffler upgrades
        if (player instanceof RouterFakePlayer && !player.level().isClientSide() && type == LevelEvent.PARTICLES_DESTROY_BLOCK) {
            cir.cancel();
        }
    }
}
