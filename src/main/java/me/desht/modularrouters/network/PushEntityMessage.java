package me.desht.modularrouters.network;

import me.desht.modularrouters.client.util.ClientUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.simple.SimpleMessage;

/**
 * Received on: CLIENT
 * Sent by server so clients promptly update an entity's velocity when it gets shoved by an extruded block.
 */
public class PushEntityMessage implements SimpleMessage {
    private final int id;
    private final Vec3 vec;

    public PushEntityMessage(Entity entity, Vec3 vec) {
        this.id = entity.getId();
        this.vec = vec;
    }

    public PushEntityMessage(FriendlyByteBuf buf) {
        id = buf.readInt();
        vec = buf.readVec3();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(id);
        buffer.writeVec3(vec);
    }

    @Override
    public void handleMainThread(NetworkEvent.Context context) {
        Entity entity = ClientUtil.theClientLevel().getEntity(id);
        if (entity != null) {
            entity.setDeltaMovement(vec.x, vec.y, vec.z);
            entity.horizontalCollision = false;
            entity.verticalCollision = false;
            if (entity instanceof LivingEntity l) l.setJumping(true);
        }
    }
}
