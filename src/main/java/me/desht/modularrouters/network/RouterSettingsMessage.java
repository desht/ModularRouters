package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Used when a player updates settings on an item router via its GUI.
 */
public class RouterSettingsMessage {
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

    public RouterSettingsMessage(PacketBuffer buffer) {
        BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        WorldServer world = MiscUtil.getWorldForDimensionId(buffer.readInt());
        if (world != null) {
            router = TileEntityItemRouter.getRouterAt(world, pos);
        }
        rrb = RouterRedstoneBehaviour.values()[buffer.readByte()];
        eco = buffer.readBoolean();
    }

    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(router.getPos().getX());
        byteBuf.writeInt(router.getPos().getY());
        byteBuf.writeInt(router.getPos().getZ());
        byteBuf.writeInt(MiscUtil.getDimensionForWorld(router.getWorld()));
        byteBuf.writeByte(rrb.ordinal());
        byteBuf.writeBoolean(eco);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (router != null) {
                router.setRedstoneBehaviour(rrb);
                router.setEcoMode(eco);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
