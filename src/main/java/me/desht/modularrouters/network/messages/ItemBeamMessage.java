package me.desht.modularrouters.network.messages;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.util.BeamData;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * Received on: CLIENT
 * <p>
 * Sent by server to play an item beam between a router and another inventory
 */
public record ItemBeamMessage(BlockPos pos, List<BeamData> beams) implements CustomPacketPayload {
    public static final ResourceLocation ID = MiscUtil.RL("item_beam");

    /**
     * Create a new beam message
     * @param te the tile entity responsible for the rendering
     * @param beams the beams(s) to send
     */
    public ItemBeamMessage(BlockEntity te, List<BeamData> beams) {
        this(te.getBlockPos(), List.copyOf(beams));
    }

    public static ItemBeamMessage fromNetwork(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ImmutableList.Builder<BeamData> builder = ImmutableList.builder();
        int n = buf.readVarInt();
        for (int i = 0; i < n; i++) {
            builder.add(new BeamData(buf, pos));
        }
        List<BeamData> beams = builder.build();
        return new ItemBeamMessage(pos, beams);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(beams.size());
        beams.forEach(beam -> beam.toBytes(buf, pos));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
