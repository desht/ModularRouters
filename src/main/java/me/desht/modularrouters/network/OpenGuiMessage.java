package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent when the client needs the server to open (or reopen) a container-based GUI.
 * 1) Reopen router GUI when installed module has been edited
 * 2) Reopen module GUI when installed filter has been edited
 * 3) Open installed module GUI
 * 4) Open installed filter GUI (if it has a container)
 */
public class OpenGuiMessage {
    private enum Operation {
        ROUTER,
        MODULE_HELD,
        MODULE_INSTALLED,
        FILTER_HELD,
        FILTER_INSTALLED
    }

    private Operation operation;
    private MFLocator locator;

    @SuppressWarnings("unused")
    public OpenGuiMessage() {
    }

    private OpenGuiMessage(Operation operation, MFLocator locator) {
        this.operation = operation;
        this.locator = locator;
    }

    OpenGuiMessage(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        operation = Operation.values()[pb.readByte()];
        locator = MFLocator.fromBuffer(pb);
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

    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeByte(operation.ordinal());
        locator.writeBuf(pb);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            assert player != null;
            Optional<TileEntityItemRouter> r = locator.routerPos == null ? Optional.empty() : TileEntityItemRouter.getRouterAt(player.getEntityWorld(), locator.routerPos);
//            final TileEntityItemRouter router = locator.routerPos != null ?
//                    TileEntityItemRouter.getRouterAt(player.getEntityWorld(), locator.routerPos) : null;
            switch (operation) {
                case ROUTER:
                    // item router GUI
                    r.ifPresent(router -> NetworkHooks.openGui(player, router, locator.routerPos));
                    break;
                case MODULE_HELD:
                    // module held in player's hand
                    NetworkHooks.openGui(player, new ItemModule.ContainerProvider(player, locator), locator::writeBuf);
                    break;
                case MODULE_INSTALLED:
                    // module installed in a router
                    r.ifPresent(router -> NetworkHooks.openGui(player, new ItemModule.ContainerProvider(player, locator), locator::writeBuf));
                    break;
                case FILTER_HELD:
                    // filter is in a module in player's hand
                    NetworkHooks.openGui(player, new ItemSmartFilter.ContainerProvider(player, locator), locator::writeBuf);
                    break;
                case FILTER_INSTALLED:
                    // filter is in a module in a router
                    r.ifPresent(router -> NetworkHooks.openGui(player, new ItemSmartFilter.ContainerProvider(player, locator), locator::writeBuf));
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
