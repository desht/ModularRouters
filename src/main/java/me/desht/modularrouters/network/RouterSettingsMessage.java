package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RouterSettingsMessage implements IMessage {
    private TileEntityItemRouter router;
    private RouterRedstoneBehaviour rrb;

    public RouterSettingsMessage() {
    }

    public RouterSettingsMessage(TileEntityItemRouter router, RouterRedstoneBehaviour rrb) {
        this.router = router;
        this.rrb = rrb;
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        BlockPos pos = new BlockPos(byteBuf.readInt(), byteBuf.readInt(), byteBuf.readInt());
        WorldServer w = DimensionManager.getWorld(byteBuf.readInt());

        if (w != null) {
            TileEntity te = w.getTileEntity(pos);
            if (te instanceof TileEntityItemRouter) {
                router = (TileEntityItemRouter) te;
                rrb = RouterRedstoneBehaviour.values()[byteBuf.readByte()];
            }
        }
//        if (w != null) {
//            TileEntity te = w.getTileEntity(pos);
//            if (te instanceof TileEntityItemRouter) {
//                ((TileEntityItemRouter) te).setRedstoneBehaviour(RouterRedstoneBehaviour.values()[byteBuf.readByte()]);
//            }
//        }
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(router.getPos().getX());
        byteBuf.writeInt(router.getPos().getY());
        byteBuf.writeInt(router.getPos().getZ());
        byteBuf.writeInt(router.getWorld().provider.getDimension());
        byteBuf.writeByte(rrb.ordinal());
    }

    public static class Handler implements IMessageHandler<RouterSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(RouterSettingsMessage msg, MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
            mainThread.addScheduledTask(() -> {
                if (msg.router != null) {
                    msg.router.setRedstoneBehaviour(msg.rrb);
                }
            });
            return null;
        }
    }
}
