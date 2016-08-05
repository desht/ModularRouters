package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Used to force a re-open of the router GUI after an installed module has been edited.
 */
public class ReopenRouterMessage implements IMessage {
    private BlockPos pos;

    public ReopenRouterMessage() {
    }

    public ReopenRouterMessage(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    public static class Handler implements IMessageHandler<ReopenRouterMessage, IMessage> {
        @Override
        public IMessage onMessage(ReopenRouterMessage message, MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
            mainThread.addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                BlockPos pos = message.pos;
                TileEntity te = player.getEntityWorld().getTileEntity(pos);
                if (te instanceof TileEntityItemRouter) {
                    player.openGui(ModularRouters.instance, ModularRouters.GUI_ROUTER, player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
                }
            });
            return null;
        }
    }
}
