package me.desht.modularrouters.network;

import me.desht.modularrouters.item.upgrade.SyncUpgrade;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.simple.SimpleMessage;

/**
 * Received on: SERVER
 *
 * Sent by client when a new tuning value is entered via Sync Upgrade GUI
 */
public class SyncUpgradeSettingsMessage implements SimpleMessage {
    private final int tunedValue;
    private final InteractionHand hand;

    public SyncUpgradeSettingsMessage(int tunedValue, InteractionHand hand) {
        this.tunedValue = tunedValue;
        this.hand = hand;
    }

    public SyncUpgradeSettingsMessage(FriendlyByteBuf buf) {
        tunedValue = buf.readInt();
        hand = buf.readEnum(InteractionHand.class);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(tunedValue);
        buffer.writeEnum(hand);
    }

    @Override
    public void handleMainThread(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof SyncUpgrade) {
                SyncUpgrade.setTunedValue(held, tunedValue);
            }
        }
    }
}
