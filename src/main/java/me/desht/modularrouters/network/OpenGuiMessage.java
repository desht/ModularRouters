package me.desht.modularrouters.network;

import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkHooks;
import net.neoforged.neoforge.network.simple.SimpleMessage;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent when the client needs the server to open (or reopen) a container-based GUI.
 * 1) Reopen router GUI when installed module has been edited
 * 2) Reopen module GUI when installed filter has been edited
 * 3) Open installed module GUI
 * 4) Open installed filter GUI (only if it is container-based)
 */
public class OpenGuiMessage implements SimpleMessage {
    private enum Operation {
        ROUTER,
        MODULE_HELD,
        MODULE_INSTALLED,
        FILTER_HELD,
        FILTER_INSTALLED
    }

    private final Operation operation;
    private final MFLocator locator;

    private OpenGuiMessage(Operation operation, MFLocator locator) {
        this.operation = operation;
        this.locator = locator;
    }

    OpenGuiMessage(FriendlyByteBuf buf) {
        operation = buf.readEnum(Operation.class);
        locator = MFLocator.fromBuffer(buf);
    }

    public static OpenGuiMessage openRouter(MFLocator locator) {
        return new OpenGuiMessage(Operation.ROUTER, locator);
    }

    public static OpenGuiMessage openModuleInHand(MFLocator locator) {
        return new OpenGuiMessage(Operation.MODULE_HELD, locator);
    }

    public static OpenGuiMessage openModuleInRouter(MFLocator locator) {
        return new OpenGuiMessage(Operation.MODULE_INSTALLED, locator);
    }

    public static OpenGuiMessage openFilterInHeldModule(MFLocator locator) {
        return new OpenGuiMessage(Operation.FILTER_HELD, locator);
    }

    public static OpenGuiMessage openFilterInInstalledModule(MFLocator locator) {
        return new OpenGuiMessage(Operation.FILTER_INSTALLED, locator);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(operation);
        locator.writeBuf(buffer);
    }

    @Override
    public void handleMainThread(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            switch (operation) {
                case ROUTER ->
                    // item router GUI
                        locator.getRouter(player.getCommandSenderWorld())
                                .ifPresent(router -> NetworkHooks.openScreen(player, router, locator.routerPos));
                case MODULE_HELD ->
                    // module held in player's hand
                        NetworkHooks.openScreen(player, new ModuleItem.ModuleMenuProvider(player, locator), locator::writeBuf);
                case MODULE_INSTALLED ->
                    // module installed in a router
                        locator.getRouter(player.getCommandSenderWorld())
                                .ifPresent(router -> NetworkHooks.openScreen(player, new ModuleItem.ModuleMenuProvider(player, locator), locator::writeBuf));
                case FILTER_HELD ->
                    // filter is in a module in player's hand
                        NetworkHooks.openScreen(player, new SmartFilterItem.FilterMenuProvider(player, locator), locator::writeBuf);
                case FILTER_INSTALLED ->
                    // filter is in a module in a router
                        locator.getRouter(player.getCommandSenderWorld())
                                .ifPresent(router -> NetworkHooks.openScreen(player, new SmartFilterItem.FilterMenuProvider(player, locator), locator::writeBuf));
            }
        }
    }


    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                switch (operation) {
                    case ROUTER ->
                            // item router GUI
                            locator.getRouter(player.getCommandSenderWorld())
                                    .ifPresent(router -> NetworkHooks.openScreen(player, router, locator.routerPos));
                    case MODULE_HELD ->
                            // module held in player's hand
                            NetworkHooks.openScreen(player, new ModuleItem.ModuleMenuProvider(player, locator), locator::writeBuf);
                    case MODULE_INSTALLED ->
                            // module installed in a router
                            locator.getRouter(player.getCommandSenderWorld())
                                    .ifPresent(router -> NetworkHooks.openScreen(player, new ModuleItem.ModuleMenuProvider(player, locator), locator::writeBuf));
                    case FILTER_HELD ->
                            // filter is in a module in player's hand
                            NetworkHooks.openScreen(player, new SmartFilterItem.FilterMenuProvider(player, locator), locator::writeBuf);
                    case FILTER_INSTALLED ->
                            // filter is in a module in a router
                            locator.getRouter(player.getCommandSenderWorld())
                                    .ifPresent(router -> NetworkHooks.openScreen(player, new SmartFilterItem.FilterMenuProvider(player, locator), locator::writeBuf));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
