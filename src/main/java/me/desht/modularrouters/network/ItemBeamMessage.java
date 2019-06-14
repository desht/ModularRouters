package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.item_beam.ItemBeam;
import me.desht.modularrouters.client.item_beam.ItemBeamDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 *
 * Sent by server to play an item beam between a router and another inventory
 */
public class ItemBeamMessage {
    private static final Vec3d HALF_BLOCK = new Vec3d(0.5, 0.5, 0.5);
    private int duration;
    private BlockPos pos1;
    private BlockPos pos2;
    private ItemStack stack;
    private int color;
    private boolean itemFade;

    public ItemBeamMessage() {
    }

    public ItemBeamMessage(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pos1 = pb.readBlockPos();
        pos2 = pb.readBlockPos();
        stack = pb.readItemStack();
        color = pb.readInt();
        itemFade = pb.readBoolean();
        duration = pb.readVarInt();
    }

    public ItemBeamMessage(BlockPos pos1, BlockPos pos2, ItemStack stack, int color, int duration) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.stack = stack;
        this.color = color;
        this.itemFade = false;
        this.duration = duration;
    }

    public ItemBeamMessage withFadeout() {
        this.itemFade = true;
        return this;
    }

    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeBlockPos(pos1);
        pb.writeBlockPos(pos2);
        pb.writeItemStack(stack);
        pb.writeInt(color);
        pb.writeBoolean(itemFade);
        pb.writeVarInt(duration);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World w = ModularRouters.proxy.theClientWorld();
            if (w != null) {
                ItemBeamDispatcher.INSTANCE.addBeam(new ItemBeam(new Vec3d(pos1).add(HALF_BLOCK), new Vec3d(pos2).add(HALF_BLOCK), stack, color, Math.max(5, duration + 1), itemFade));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
