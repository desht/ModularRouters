package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

/**
 * Received on: SERVER
 * <p>
 * Sent by client when a module is left-clicked; ask the server to validate the module and
 * send the player a message.
 */
public record ValidateModuleMessage(InteractionHand hand) implements CustomPacketPayload {
    public static final ResourceLocation ID = MiscUtil.RL("validate_module");

    public ValidateModuleMessage(FriendlyByteBuf buf) {
        this(buf.readEnum(InteractionHand.class));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
