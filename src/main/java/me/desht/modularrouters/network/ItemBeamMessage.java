package me.desht.modularrouters.network;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 *
 * Sent by server to play an item beam between a router and another inventory
 */
public class ItemBeamMessage {
    private final BlockPos pos1;
    private final List<BeamData> beams;

    /**
     * Create a new beam message
     * @param te the tile entity responsible for the rendering
     * @param beams the beams(s) to send
     */
    public ItemBeamMessage(TileEntity te, List<BeamData> beams) {
        this.pos1 = te.getBlockPos();
        this.beams = beams;
    }

    public ItemBeamMessage(PacketBuffer buf) {
        pos1 = buf.readBlockPos();
        ImmutableList.Builder<BeamData> builder = ImmutableList.builder();
        int n = buf.readVarInt();
        for (int i = 0; i < n; i++) {
            builder.add(new BeamData(buf, pos1));
        }
        this.beams = builder.build();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos1);
        buf.writeVarInt(beams.size());
        beams.forEach(beam -> beam.toBytes(buf, pos1));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> TileEntityItemRouter.getRouterAt(ClientUtil.theClientWorld(), pos1).ifPresent(te -> {
            beams.forEach(te::addItemBeam);
        }));
        ctx.get().setPacketHandled(true);
    }

}
