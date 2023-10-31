package me.desht.modularrouters.network;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.simple.SimpleMessage;

import java.util.List;

/**
 * Received on: CLIENT
 *
 * Sent by server to play an item beam between a router and another inventory
 */
public class ItemBeamMessage implements SimpleMessage {
    private final BlockPos pos1;
    private final List<BeamData> beams;

    /**
     * Create a new beam message
     * @param te the tile entity responsible for the rendering
     * @param beams the beams(s) to send
     */
    public ItemBeamMessage(BlockEntity te, List<BeamData> beams) {
        this.pos1 = te.getBlockPos();
        this.beams = beams;
    }

    public ItemBeamMessage(FriendlyByteBuf buf) {
        pos1 = buf.readBlockPos();
        ImmutableList.Builder<BeamData> builder = ImmutableList.builder();
        int n = buf.readVarInt();
        for (int i = 0; i < n; i++) {
            builder.add(new BeamData(buf, pos1));
        }
        this.beams = builder.build();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos1);
        buf.writeVarInt(beams.size());
        beams.forEach(beam -> beam.toBytes(buf, pos1));
    }

    @Override
    public void handleMainThread(NetworkEvent.Context context) {
        ClientUtil.theClientLevel().getBlockEntity(pos1, ModBlockEntities.MODULAR_ROUTER.get())
                .ifPresent(te -> beams.forEach(te::addItemBeam));
    }
}
