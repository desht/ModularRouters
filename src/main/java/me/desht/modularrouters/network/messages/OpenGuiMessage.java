package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.network.OpenGuiOp;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Received on: SERVER
 * <p>
 * Sent when the client needs the server to open (or reopen) a container-based GUI.
 * 1) Reopen router GUI when installed module has been edited
 * 2) Reopen module GUI when installed filter has been edited
 * 3) Open installed module GUI
 * 4) Open installed filter GUI (only if it is container-based)
 */
public record OpenGuiMessage(OpenGuiOp op, MFLocator locator) implements CustomPacketPayload {
    public static final ResourceLocation ID = MiscUtil.RL("open_gui");

    public OpenGuiMessage(FriendlyByteBuf buf) {
        this(buf.readEnum(OpenGuiOp.class), MFLocator.fromBuffer(buf));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(op);
        locator.writeBuf(buffer);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static OpenGuiMessage openRouter(MFLocator locator) {
        return new OpenGuiMessage(OpenGuiOp.ROUTER, locator);
    }

    public static OpenGuiMessage openModuleInHand(MFLocator locator) {
        return new OpenGuiMessage(OpenGuiOp.MODULE_HELD, locator);
    }

    public static OpenGuiMessage openModuleInRouter(MFLocator locator) {
        return new OpenGuiMessage(OpenGuiOp.MODULE_INSTALLED, locator);
    }

    public static OpenGuiMessage openFilterInHeldModule(MFLocator locator) {
        return new OpenGuiMessage(OpenGuiOp.FILTER_HELD, locator);
    }

    public static OpenGuiMessage openFilterInInstalledModule(MFLocator locator) {
        return new OpenGuiMessage(OpenGuiOp.FILTER_INSTALLED, locator);
    }
}
