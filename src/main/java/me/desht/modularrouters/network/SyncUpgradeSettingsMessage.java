package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.item.upgrade.SyncUpgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncUpgradeSettingsMessage implements IMessage {
    private int tunedValue;

    public SyncUpgradeSettingsMessage() {
    }

    public SyncUpgradeSettingsMessage(int tunedValue) {
        this.tunedValue = tunedValue;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tunedValue = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(tunedValue);
    }

    public static class Handler implements IMessageHandler<SyncUpgradeSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(SyncUpgradeSettingsMessage message, MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.getEntityWorld();
            mainThread.addScheduledTask(() -> {
                // TODO only works for player main hand right now
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
                if (ItemUpgrade.isType(held, ItemUpgrade.UpgradeType.SYNC)) {
                    SyncUpgrade.setTunedValue(held, message.tunedValue);
                }
            });
            return null;
        }
    }
}
