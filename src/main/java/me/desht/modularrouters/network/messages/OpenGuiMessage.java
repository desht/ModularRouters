package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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
    public static final Type<OpenGuiMessage> TYPE = new Type<>(MiscUtil.RL("open_gui"));

    public static final StreamCodec<FriendlyByteBuf,OpenGuiMessage> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(OpenGuiOp.class), OpenGuiMessage::op,
            MFLocator.STREAM_CODEC, OpenGuiMessage::locator,
            OpenGuiMessage::new
    );

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(OpenGuiMessage message, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        MFLocator locator = message.locator();
        switch (message.op()) {
            case ROUTER ->
                // item router GUI
                    locator.getRouter(player.level())
                            .ifPresent(router -> player.openMenu(router, locator.routerPos()));
            case MODULE_HELD ->
                // module held in player's hand
                    player.openMenu(new ModuleItem.ModuleMenuProvider(player, locator), locator::toNetwork);
            case MODULE_INSTALLED ->
                // module installed in a router
                    locator.getRouter(player.level())
                            .ifPresent(router -> player.openMenu(new ModuleItem.ModuleMenuProvider(player, locator), locator::toNetwork));
            case FILTER_HELD ->
                // filter is in a module in player's hand
                    player.openMenu(new SmartFilterItem.FilterMenuProvider(player, locator), locator::toNetwork);
            case FILTER_INSTALLED ->
                // filter is in a module in a router
                    locator.getRouter(player.level())
                            .ifPresent(router -> player.openMenu(new SmartFilterItem.FilterMenuProvider(player, locator), locator::toNetwork));
        }
    }

    public enum OpenGuiOp {
        ROUTER,
        MODULE_HELD,
        MODULE_INSTALLED,
        FILTER_HELD,
        FILTER_INSTALLED
    }
}
