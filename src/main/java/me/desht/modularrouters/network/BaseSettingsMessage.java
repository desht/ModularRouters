package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class BaseSettingsMessage implements IMessage {
    protected BlockPos routerPos;
    protected EnumHand hand;
    protected int moduleSlotIndex;
    protected NBTTagCompound extData;

    public BaseSettingsMessage() {
    }

    public BaseSettingsMessage(BlockPos routerPos, EnumHand hand, int moduleSlotIndex, NBTTagCompound extData) {
        this.routerPos = routerPos;
        this.moduleSlotIndex = moduleSlotIndex;
        this.hand = hand;
        this.extData = extData;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            routerPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            moduleSlotIndex = buf.readByte();
        } else {
            hand = EnumHand.values()[buf.readByte()];
        }
        extData = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (routerPos == null) {
            buf.writeBoolean(false);
            buf.writeByte(hand.ordinal());
        } else {
            buf.writeBoolean(true);
            writePos(buf, routerPos);
            buf.writeByte(moduleSlotIndex);
        }
        ByteBufUtils.writeTag(buf, extData);
    }

    protected void writePos(ByteBuf buf, BlockPos pos) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    protected BlockPos readPos(ByteBuf buf) {
        return new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public NBTTagCompound getExtData() {
        return extData;
    }
}
