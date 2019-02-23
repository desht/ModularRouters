package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.item.upgrade.SyncUpgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncUpgradeSettingsMessage {
    private int tunedValue;
    private EnumHand hand; // TODO offhand not supported yet

    public SyncUpgradeSettingsMessage() {
    }

    public SyncUpgradeSettingsMessage(int tunedValue) {
        this.tunedValue = tunedValue;
        this.hand = EnumHand.MAIN_HAND;
    }

    public SyncUpgradeSettingsMessage(ByteBuf buf) {
        tunedValue = buf.readInt();
        hand = buf.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(tunedValue);
        buf.writeBoolean(hand == EnumHand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            EntityPlayer player = ctx.get().getSender();
            ItemStack held = player.getHeldItem(hand);
            if (held.getItem() instanceof SyncUpgrade) {
                SyncUpgrade.setTunedValue(held, tunedValue);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
