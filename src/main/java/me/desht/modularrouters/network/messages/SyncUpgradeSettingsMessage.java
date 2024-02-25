package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

/**
 * Received on: SERVER
 * <p>
 * Sent by client when a new tuning value is entered via Sync Upgrade GUI
 */
public record SyncUpgradeSettingsMessage(int tunedValue, InteractionHand hand) implements CustomPacketPayload {
    public static final ResourceLocation ID = MiscUtil.RL("sync_upgrade_settings");

    public SyncUpgradeSettingsMessage(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readEnum(InteractionHand.class));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(tunedValue);
        buffer.writeEnum(hand);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
