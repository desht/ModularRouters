package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RouterActiveMessage implements IMessage {
    private BlockPos pos;
    private boolean active;

    public RouterActiveMessage() {}

    public RouterActiveMessage(BlockPos pos, boolean active) {
        this.pos = pos;
        this.active = active;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        active = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeBoolean(active);
    }

    public static class Handler implements IMessageHandler<RouterActiveMessage, IMessage> {
        @Override
        public IMessage onMessage(RouterActiveMessage msg, MessageContext ctx) {
            World w = ModularRouters.proxy.theClientWorld();
            if (w != null) {
                ModularRouters.proxy.threadListener().addScheduledTask(() -> {
                    TileEntity te = w.getTileEntity(msg.pos);
                    if (te instanceof TileEntityItemRouter) {
                        ((TileEntityItemRouter) te).setActiveState(msg.active);
                    }
                });
            }
            return null;
        }
    }
}
