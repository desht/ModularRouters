package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Received on: SERVER
 * <p>
 * Sent by client when a player updates a module's settings via its GUI.
 */
public record ModuleSettingsMessage(MFLocator locator, CompoundTag payload) implements CustomPacketPayload {
    public static final ResourceLocation ID = MiscUtil.RL("module_settings");

    public ModuleSettingsMessage(FriendlyByteBuf buf) {
        this(MFLocator.fromBuffer(buf), buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        locator.writeBuf(buffer);
        buffer.writeNbt(payload);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
