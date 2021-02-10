package me.desht.modularrouters.network;

import me.desht.modularrouters.client.util.ClientUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server so clients promptly update an entity's velocity when it gets shoved by an extruded block.
 */
public class PushEntityMessage {
    private final int id;
    private final Vector3d vec;

    public PushEntityMessage(Entity entity, Vector3d vec) {
        this.id = entity.getEntityId();
        this.vec = vec;
    }

    public PushEntityMessage(PacketBuffer buf) {
        id = buf.readInt();
        vec = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(id);
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World w = ClientUtil.theClientWorld();
            Entity entity = w.getEntityByID(id);
            if (entity != null) {
                entity.setMotion(vec.x, vec.y, vec.z);
                entity.collidedHorizontally = false;
                entity.collidedVertically = false;
                if (entity instanceof LivingEntity) ((LivingEntity) entity).setJumping(true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
