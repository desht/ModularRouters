package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
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
    private int filterSlotIndex;

    public OpenGuiMessage() {
    }

    private OpenGuiMessage(What what, BlockPos pos, EnumHand hand, int moduleSlotIndex, int filterSlotIndex) {
        super(pos, hand, moduleSlotIndex, null);
        this.what = what;
        this.filterSlotIndex = filterSlotIndex;
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

    public static OpenGuiMessage openFilterInModule(EnumHand hand, int filterSlotIndex) {
        return new OpenGuiMessage(What.FILTER_HELD, null, hand, -1, filterSlotIndex);
    }

    public static OpenGuiMessage openFilterInInstalledModule(BlockPos routerPos, int moduleSlotIndex, int filterSlotIndex) {
        return new OpenGuiMessage(What.FILTER_INSTALLED, routerPos, null, moduleSlotIndex, filterSlotIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
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

    public static class Handler implements IMessageHandler<OpenGuiMessage, IMessage> {
        @Override
        public IMessage onMessage(OpenGuiMessage message, MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.getEntityWorld();
            mainThread.addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                BlockPos pos = player.getPosition();
                TileEntityItemRouter router = null;
                if (message.routerPos != null) {
                    pos = message.routerPos;
                    router = TileEntityItemRouter.getRouterAt(player.getEntityWorld(), pos);
                }
                switch (message.what) {
                    case ROUTER:
                        if (router != null) {
                            player.openGui(ModularRouters.instance,
                                    ModularRouters.GUI_ROUTER,
                                    player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
                        }
                        break;
                    case MODULE_HELD:
                        player.openGui(ModularRouters.instance,
                                message.hand == EnumHand.MAIN_HAND ? ModularRouters.GUI_MODULE_HELD_MAIN : ModularRouters.GUI_MODULE_HELD_OFF,
                                player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
                        break;
                    case MODULE_INSTALLED:
                        if (router != null) {
                            router.playerConfiguringModule(player, message.moduleSlotIndex);
                            player.openGui(ModularRouters.instance,
                                    ModularRouters.GUI_MODULE_INSTALLED,
                                    player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
                        }
                        break;
                    case FILTER_HELD:
                        // filter is in a module in player's hand
                        // record the filter slot in the module itemstack's NBT - client needs this when creating the GUI
                        ModuleHelper.setFilterConfigSlot(player.getHeldItem(message.hand), message.filterSlotIndex);
                        player.openGui(ModularRouters.instance,
                                message.hand == EnumHand.MAIN_HAND ? ModularRouters.GUI_FILTER_HELD_MAIN : ModularRouters.GUI_FILTER_HELD_OFF,
                                player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
                        break;
                    case FILTER_INSTALLED:
                        if (router != null) {
                            // filter is in a module in a router
                            router.playerConfiguringModule(player, message.moduleSlotIndex, message.filterSlotIndex);
                            player.openGui(ModularRouters.instance,
                                    ModularRouters.GUI_FILTER_INSTALLED,
                                    player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
                        }
                        break;
                }
            });
            return null;
        }
    }
}
