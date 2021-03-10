package me.desht.modularrouters.network;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: BOTH
 *
 * Sent by client to update router settings from GUI
 * Sent by server to sync router settings when GUI is opened
 */
public class RouterSettingsMessage {
    private final boolean eco;
    private final RouterRedstoneBehaviour redstoneBehaviour;
    private final BlockPos pos;

    public RouterSettingsMessage(TileEntityItemRouter router) {
        this.pos = router.getBlockPos();
        this.redstoneBehaviour = router.getRedstoneBehaviour();
        this.eco = router.getEcoMode();
    }

    RouterSettingsMessage(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        redstoneBehaviour = RouterRedstoneBehaviour.values()[buffer.readByte()];
        eco = buffer.readBoolean();
    }

    public void toBytes(PacketBuffer byteBuf) {
        byteBuf.writeBlockPos(pos);
        byteBuf.writeByte(redstoneBehaviour.ordinal());
        byteBuf.writeBoolean(eco);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World w = ctx.get().getSender() == null ? ClientUtil.theClientWorld() : ctx.get().getSender().getLevel();
            TileEntityItemRouter.getRouterAt(w, pos).ifPresent(router -> {
                router.setRedstoneBehaviour(redstoneBehaviour);
                router.setEcoMode(eco);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
