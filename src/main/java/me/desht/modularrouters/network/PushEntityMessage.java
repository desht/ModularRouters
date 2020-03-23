package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.client.util.ClientUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server so clients promptly update an entity's velocity when it gets shoved by an extruded block.
 */
public class PushEntityMessage {
    private int id;
    private double x;
    private double y;
    private double z;

    public PushEntityMessage() {
    }

    public PushEntityMessage(Entity entity, double x, double y, double z) {
        this.id = entity.getEntityId();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PushEntityMessage(ByteBuf buf) {
        id = buf.readInt();
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World w = ClientUtil.theClientWorld();
            Entity entity = w.getEntityByID(id);
            if (entity != null) {
                entity.setMotion(x, y, z);
                entity.onGround = false;
                entity.collided = false;
                entity.collidedHorizontally = false;
                entity.collidedVertically = false;
                if (entity instanceof LivingEntity) ((LivingEntity) entity).setJumping(true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
