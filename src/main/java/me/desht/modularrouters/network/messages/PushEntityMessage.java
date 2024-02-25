package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Received on: CLIENT
 * <p>
 * Sent by server so clients promptly update an entity's velocity when it gets shoved by an extruded block.
 */
public record PushEntityMessage(int entityId, Vec3 vec) implements CustomPacketPayload {
    public static final ResourceLocation ID = MiscUtil.RL("push_entity");

    public PushEntityMessage(Entity entity, Vec3 vec) {
        this(entity.getId(), vec);
    }

    public PushEntityMessage(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readVec3());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeVec3(vec);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
