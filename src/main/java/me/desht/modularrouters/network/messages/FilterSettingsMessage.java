package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.FilterOp;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Optional;

/**
 * Received on: SERVER
 * <p>
 * Sent when a filter's settings have been changed in any way via its GUI.
 * The filter could be in a player's hand, or in a module (which may or may not be in a router...)
 */
public record FilterSettingsMessage(FilterOp op, MFLocator locator, CompoundTag payload) implements CustomPacketPayload {
    public static final ResourceLocation ID = MiscUtil.RL("filter_settings");

    public FilterSettingsMessage(FriendlyByteBuf buf) {
        this(buf.readEnum(FilterOp.class), MFLocator.fromBuffer(buf), buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(op);
        locator.writeBuf(buffer);
        buffer.writeNbt(payload);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public Optional<IItemHandler> getTargetInventory() {
        return ModuleTarget.fromNBT(payload).getItemHandler();
    }

}
