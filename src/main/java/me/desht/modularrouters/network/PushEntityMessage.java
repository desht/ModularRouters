package me.desht.modularrouters.network;

import me.desht.modularrouters.client.util.ClientUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server so clients promptly update an entity's velocity when it gets shoved by an extruded block.
 */
public class PushEntityMessage {
    private final int id;
    private final Vec3 vec;

    public PushEntityMessage(Entity entity, Vec3 vec) {
        this.id = entity.getId();
        this.vec = vec;
    }

    public PushEntityMessage(FriendlyByteBuf buf) {
        id = buf.readInt();
        vec = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level w = ClientUtil.theClientWorld();
            Entity entity = w.getEntity(id);
            if (entity != null) {
                entity.setDeltaMovement(vec.x, vec.y, vec.z);
                entity.horizontalCollision = false;
                entity.verticalCollision = false;
                if (entity instanceof LivingEntity l) l.setJumping(true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
