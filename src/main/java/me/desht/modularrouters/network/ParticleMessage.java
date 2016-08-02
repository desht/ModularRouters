package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ParticleMessage implements IMessage {
    private EnumParticleTypes type;
    private int dimension;
    private double x;
    private double y;
    private double z;
    private double xSpeed;
    private double ySpeed;
    private double zSpeed;
    private int[] data;

    public ParticleMessage() {
    }

    public ParticleMessage(EnumParticleTypes type, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... data) {
        this.type = type;
        this.dimension = world.provider.getDimension();
        this.x = x;
        this.y = y;
        this.z = z;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.zSpeed = zSpeed;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        type = EnumParticleTypes.values()[buf.readInt()];
        dimension = buf.readInt();
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        xSpeed = buf.readDouble();
        ySpeed = buf.readDouble();
        zSpeed = buf.readDouble();
        int l = buf.readInt();
        data = new int[l + 1];
        for (int i = 0; i < l; i++) {
            data[l] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(type.ordinal());
        buf.writeInt(dimension);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(xSpeed);
        buf.writeDouble(ySpeed);
        buf.writeDouble(zSpeed);
        buf.writeInt(data.length);
        for (int d : data) {
            buf.writeInt(d);
        }
    }

    public static class Handler implements IMessageHandler<ParticleMessage, IMessage> {
        @Override
        public IMessage onMessage(ParticleMessage msg, MessageContext ctx) {
            WorldServer w = DimensionManager.getWorld(msg.dimension);
            if (w != null) {
//                System.out.println(String.format("particles! %s %f,%f,%f %f,%f,%f", msg.type, msg.x, msg.y, msg.z, msg.xSpeed, msg.ySpeed, msg.zSpeed));
                w.spawnParticle(msg.type, msg.x, msg.y, msg.z, msg.xSpeed, msg.ySpeed, msg.zSpeed, msg.data);
            }
            return null;
        }
    }
}
