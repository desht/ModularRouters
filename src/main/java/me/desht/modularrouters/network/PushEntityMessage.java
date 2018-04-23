package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class PushEntityMessage implements IMessage {
    private int id;
    private double x;
    private double y;
    private double z;

    public PushEntityMessage() {
        x = y = z = 0;
    }

    public PushEntityMessage(Entity entity, double x, double y, double z) {
        this.id = entity.getEntityId();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    public static class Handler implements IMessageHandler<PushEntityMessage, IMessage> {

        @Override
        public IMessage onMessage(PushEntityMessage message, MessageContext ctx) {
            World w = ModularRouters.proxy.theClientWorld();
            if (w != null) {
                Entity entity = w.getEntityByID(message.id);
                if (entity != null) {
                    entity.motionX = message.x;
                    entity.motionY = message.y;
                    entity.motionZ = message.z;

                    entity.onGround = false;
                    entity.collided = false;
                    entity.collidedHorizontally = false;
                    entity.collidedVertically = false;
                    if (entity instanceof EntityLivingBase) ((EntityLivingBase) entity).setJumping(true);
                }
            }
            return null;
        }
    }
}
