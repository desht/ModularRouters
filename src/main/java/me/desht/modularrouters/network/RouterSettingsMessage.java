package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Used when a player updates settings on an item router via its GUI.
 */
public class RouterSettingsMessage implements IMessage {
    private boolean eco;
    private TileEntityItemRouter router;
    private RouterRedstoneBehaviour rrb;

    public RouterSettingsMessage() {
    }

    public RouterSettingsMessage(TileEntityItemRouter router) {
        this.router = router;
        this.rrb = router.getRedstoneBehaviour();
        this.eco = router.getEcoMode();
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        BlockPos pos = new BlockPos(byteBuf.readInt(), byteBuf.readInt(), byteBuf.readInt());
        WorldServer world = DimensionManager.getWorld(byteBuf.readInt());
        if (world != null) {
            router = TileEntityItemRouter.getRouterAt(world, pos);
        }
        rrb = RouterRedstoneBehaviour.values()[byteBuf.readByte()];
        eco = byteBuf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(router.getPos().getX());
        byteBuf.writeInt(router.getPos().getY());
        byteBuf.writeInt(router.getPos().getZ());
        byteBuf.writeInt(router.getWorld().provider.getDimension());
        byteBuf.writeByte(rrb.ordinal());
        byteBuf.writeBoolean(eco);
    }

    public static class Handler implements IMessageHandler<RouterSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(RouterSettingsMessage msg, MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.getEntityWorld();
            mainThread.addScheduledTask(() -> {
                if (msg.router != null) {
                    msg.router.setRedstoneBehaviour(msg.rrb);
                    msg.router.setEcoMode(msg.eco);
                }
            });
            return null;
        }
    }
}
