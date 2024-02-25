package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity.EnergyDirection;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Received on: BOTH
 *  <p>
 * Sent by client to update router settings from GUI
 *  <p>
 * Sent by server to sync router settings when GUI is opened
 */
public record RouterSettingsMessage(boolean ecoMode, RouterRedstoneBehaviour redstoneBehaviour, EnergyDirection energyDirection, BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = MiscUtil.RL("router_settings");

    public RouterSettingsMessage(ModularRouterBlockEntity router) {
        this(router.getEcoMode(), router.getRedstoneBehaviour(), router.getEnergyDirection(), router.getBlockPos());
    }

    public RouterSettingsMessage(FriendlyByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readEnum(RouterRedstoneBehaviour.class), buffer.readEnum(EnergyDirection.class), buffer.readBlockPos());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeByte(redstoneBehaviour.ordinal());
        buffer.writeBoolean(ecoMode);
        buffer.writeEnum(energyDirection);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
