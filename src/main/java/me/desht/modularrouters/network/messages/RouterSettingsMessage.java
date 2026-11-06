package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity.EnergyDirection;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.logic.settings.RedstoneBehaviour;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Received on: BOTH
 *  <p>
 * Sent by client to update router settings from GUI
 *  <p>
 * Sent by server to sync router settings when GUI is opened
 */
public record RouterSettingsMessage(boolean ecoMode, RedstoneBehaviour redstoneBehaviour,
                                    EnergyDirection energyDirection, BlockPos pos) implements CustomPacketPayload
{
    public static final Type<RouterSettingsMessage> TYPE = new Type<>(MiscUtil.RL("router_settings"));

    public static final StreamCodec<FriendlyByteBuf,RouterSettingsMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, RouterSettingsMessage::ecoMode,
            NeoForgeStreamCodecs.enumCodec(RedstoneBehaviour.class), RouterSettingsMessage::redstoneBehaviour,
            NeoForgeStreamCodecs.enumCodec(EnergyDirection.class), RouterSettingsMessage::energyDirection,
            BlockPos.STREAM_CODEC, RouterSettingsMessage::pos,
            RouterSettingsMessage::new
    );

    public static RouterSettingsMessage forRouter(ModularRouterBlockEntity router) {
        return new RouterSettingsMessage(router.getEcoMode(), router.getRedstoneBehaviour(), router.getEnergyDirection(), router.getBlockPos());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(RouterSettingsMessage message, IPayloadContext context) {
        Level level = context.player().level();
        if (level.isLoaded(message.pos())) {
            level.getBlockEntity(message.pos(), ModBlockEntities.MODULAR_ROUTER.get()).ifPresent(router -> {
                if (context.player().canInteractWithBlock(router.getBlockPos(), 4.0)) {
                    router.setRedstoneBehaviour(message.redstoneBehaviour());
                    router.setEcoMode(message.ecoMode());
                    router.setEnergyDirection(message.energyDirection());
                }
            });
        }
    }
}
