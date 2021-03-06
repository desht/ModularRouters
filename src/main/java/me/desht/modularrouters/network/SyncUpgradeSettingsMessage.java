package me.desht.modularrouters.network;

import me.desht.modularrouters.item.upgrade.SyncUpgrade;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent by client when a new tuning value is entered via Sync Upgrade GUI
 */
public class SyncUpgradeSettingsMessage {
    private final int tunedValue;
    private final Hand hand;

    public SyncUpgradeSettingsMessage(int tunedValue, Hand hand) {
        this.tunedValue = tunedValue;
        this.hand = hand;
    }

    public SyncUpgradeSettingsMessage(PacketBuffer buf) {
        tunedValue = buf.readInt();
        hand = buf.readEnum(Hand.class);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(tunedValue);
        buf.writeEnum(hand);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
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
