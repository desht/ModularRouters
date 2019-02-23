package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public abstract class BaseSettingsMessage {
    BlockPos routerPos;
    EnumHand hand;
    NBTTagCompound nbtData;

    BaseSettingsMessage() {
    }

    BaseSettingsMessage(BlockPos routerPos, EnumHand hand, NBTTagCompound nbtData) {
        this.routerPos = routerPos;
        this.hand = hand;
        this.nbtData = nbtData;
    }

    BaseSettingsMessage(ByteBuf buf) {
        if (buf.readBoolean()) {
            routerPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        } else {
            hand = EnumHand.values()[buf.readByte()];
        }
        nbtData = new PacketBuffer(buf).readCompoundTag();
    }

    public void toBytes(ByteBuf buf) {
        if (routerPos == null) {
            buf.writeBoolean(false);
            buf.writeByte(hand.ordinal());
        } else {
            buf.writeBoolean(true);
            writePos(buf, routerPos);
        }
        new PacketBuffer(buf).writeCompoundTag(nbtData);
    }

    void writePos(ByteBuf buf, BlockPos pos) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    BlockPos readPos(ByteBuf buf) {
        return new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public NBTTagCompound getNbtData() {
        return nbtData;
    }
}
