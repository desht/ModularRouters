package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

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
public class OpenGuiMessage extends BaseSettingsMessage {
    private enum What {
        ROUTER,
        MODULE_HELD,
        MODULE_INSTALLED,
        FILTER_HELD,
        FILTER_INSTALLED
    }

    private What what;
    private int moduleSlotIndex;
    private int filterSlotIndex;

    public OpenGuiMessage() {
    }

    private OpenGuiMessage(What what, BlockPos pos, EnumHand hand, int moduleSlotIndex, int filterSlotIndex) {
        super(pos, hand, null);
        this.what = what;
        this.moduleSlotIndex = moduleSlotIndex;
        this.filterSlotIndex = filterSlotIndex;
    }

    OpenGuiMessage(ByteBuf buf) {
        what = What.values()[buf.readByte()];
        switch (what) {
            case ROUTER:
                routerPos = readPos(buf);
                break;
            case MODULE_HELD:
                hand = EnumHand.values()[buf.readByte()];
                break;
            case MODULE_INSTALLED:
                routerPos = readPos(buf);
                moduleSlotIndex = (int) buf.readByte();
                break;
            case FILTER_HELD:
                hand = EnumHand.values()[buf.readByte()];
                filterSlotIndex = buf.readByte();
                break;
            case FILTER_INSTALLED:
                routerPos = readPos(buf);
                moduleSlotIndex = (int) buf.readByte();
                filterSlotIndex = (int) buf.readByte();
                break;
        }
    }

    public static OpenGuiMessage openRouter(BlockPos pos) {
        return new OpenGuiMessage(What.ROUTER, pos, null, -1, -1);
    }

    public static OpenGuiMessage openModuleInHand(EnumHand hand) {
        return new OpenGuiMessage(What.MODULE_HELD, null, hand, -1, -1);
    }

    public static OpenGuiMessage openModuleInRouter(BlockPos routerPos, Integer moduleSlotIndex) {
        return new OpenGuiMessage(What.MODULE_INSTALLED, routerPos, null, moduleSlotIndex, -1);
    }

    public static OpenGuiMessage openFilterInHeldModule(EnumHand hand, int filterSlotIndex) {
        return new OpenGuiMessage(What.FILTER_HELD, null, hand, -1, filterSlotIndex);
    }

    public static OpenGuiMessage openFilterInInstalledModule(BlockPos routerPos, int moduleSlotIndex, int filterSlotIndex) {
        return new OpenGuiMessage(What.FILTER_INSTALLED, routerPos, null, moduleSlotIndex, filterSlotIndex);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(what.ordinal());
        switch (what) {
            case ROUTER:
                writePos(buf, routerPos);
                break;
            case MODULE_HELD:
                buf.writeByte(hand.ordinal());
                break;
            case MODULE_INSTALLED:
                writePos(buf, routerPos);
                buf.writeByte(moduleSlotIndex);
                break;
            case FILTER_HELD:
                buf.writeByte(hand.ordinal());
                buf.writeByte(filterSlotIndex);
                break;
            case FILTER_INSTALLED:
                writePos(buf, routerPos);
                buf.writeByte(moduleSlotIndex);
                buf.writeByte(filterSlotIndex);
                break;
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            EntityPlayerMP player = ctx.get().getSender();
            final TileEntityItemRouter router = routerPos != null ? TileEntityItemRouter.getRouterAt(player.getEntityWorld(), routerPos) : null;
            SlotTracker tracker = SlotTracker.getInstance(player);
            switch (what) {
                case ROUTER:
                    // item router GUI
                    if (router != null) {
                        NetworkHooks.openGui(player, router, buf -> buf.writeBlockPos(routerPos));
                    }
                    break;
                case MODULE_HELD:
                    // module held in player's hand
                    NetworkHooks.openGui(player, new ItemModule.ContainerProvider(hand),
                            buf -> buf.writeBoolean(hand == EnumHand.MAIN_HAND));
                case MODULE_INSTALLED:
                    // module installed in a router
                    if (router != null) {
                        tracker.setModuleSlot(moduleSlotIndex);
                        NetworkHooks.openGui(player, new ItemModule.ContainerProvider(routerPos),
                                buf -> buf.writeBlockPos(routerPos));
                    }
                    break;
                case FILTER_HELD:
                    // filter is in a module in player's hand
                    // record the filter slot in the module itemstack's NBT - client needs this when creating the GUI
                    tracker.setFilterSlot(filterSlotIndex);
                    NetworkHooks.openGui(player, new ItemSmartFilter.ContainerProvider(hand),
                             buf -> buf.writeBoolean(hand == EnumHand.MAIN_HAND));
                    break;
                case FILTER_INSTALLED:
                    // filter is in a module in a router
                    if (router != null) {
                        tracker.setModuleSlot(moduleSlotIndex);
                        tracker.setFilterSlot(filterSlotIndex);
                        NetworkHooks.openGui(player, new ItemSmartFilter.ContainerProvider(routerPos),
                                buf -> buf.writeBlockPos(routerPos));
                    }
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
