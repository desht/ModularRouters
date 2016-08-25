package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.Module.RelativeDirection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Used to send router blockstate information from server to client.
 */
public class RouterBlockstateMessage implements IMessage {
    private BlockPos pos;
    private boolean active;
    private byte openSides;

    public RouterBlockstateMessage() {}

    public RouterBlockstateMessage(BlockPos pos, TileEntityItemRouter router) {
        this.pos = pos;
        this.active = router.isActive();
        this.openSides = router.getSidesOpen();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        active = buf.readBoolean();
        openSides = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeBoolean(active);
        buf.writeByte(openSides);
    }

    public static class Handler implements IMessageHandler<RouterBlockstateMessage, IMessage> {
        @Override
        public IMessage onMessage(RouterBlockstateMessage msg, MessageContext ctx) {
            World w = ModularRouters.proxy.theClientWorld();
            if (w != null) {
                ModularRouters.proxy.threadListener().addScheduledTask(() -> {
                    TileEntity te = w.getTileEntity(msg.pos);
                    if (te instanceof TileEntityItemRouter) {
                        TileEntityItemRouter router = (TileEntityItemRouter) te;
                        router.setActiveState(msg.active);
                        router.setSidesOpen(msg.openSides);
                    }
                });
            }
            return null;
        }
    }
}
