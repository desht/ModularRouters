package me.desht.modularrouters.network;

import me.desht.modularrouters.item.upgrade.SyncUpgrade;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent by client when a new tuning value is entered via Sync Upgrade GUI
 */
public class SyncUpgradeSettingsMessage {
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

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(tunedValue);
        buf.writeEnum(hand);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (player != null) {
                ItemStack held = player.getItemInHand(hand);
                if (held.getItem() instanceof SyncUpgrade) {
                    SyncUpgrade.setTunedValue(held, tunedValue);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
